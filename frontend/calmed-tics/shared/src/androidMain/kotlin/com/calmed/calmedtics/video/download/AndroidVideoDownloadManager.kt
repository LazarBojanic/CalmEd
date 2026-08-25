package com.calmed.calmedtics.video.download

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadCursor
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.calmed.calmedtics.service.specification.VideoDownloadState
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.downloadKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.security.MessageDigest
import androidx.core.net.toUri
import org.json.JSONObject

@OptIn(UnstableApi::class)
class AndroidVideoDownloadManager(context: Context) {

    private val applicationContext = context.applicationContext

    private val manager: DownloadManager =
        DownloadUtil.getDownloadManager(applicationContext)

    private val _states =
        MutableStateFlow<Map<String, VideoDownloadState>>(emptyMap())

    val states: StateFlow<Map<String, VideoDownloadState>> = _states

    private val _downloadedUrls =
        MutableStateFlow<List<String>>(emptyList())

    val downloadedUrls: StateFlow<List<String>> = _downloadedUrls

    private val progressHandler = Handler(Looper.getMainLooper())

    private var progressLoopScheduled = false

    private val progressRunnable = object : Runnable {
        override fun run() {
            progressLoopScheduled = false
            refreshCurrentDownloads()
            ensureProgressLoop()
        }
    }

    init {
        manager.resumeDownloads()
        synchronizeWithDownloadIndex()

        manager.addListener(
            object : DownloadManager.Listener {

                override fun onInitialized(downloadManager: DownloadManager) {
                    synchronizeWithDownloadIndex()
                }

                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    updateState(download)
                }

                override fun onDownloadRemoved(
                    downloadManager: DownloadManager,
                    download: Download
                ) {
                    val url = requestUrl(download.request)

                    _states.update { current ->
                        val updated = current - url
                        recalculateDownloadedUrls(updated)
                        updated
                    }

                    ensureProgressLoop()
                }
            }
        )

        ensureProgressLoop()
    }

    fun refresh(url: String) {
        val key = downloadKey(url)
        val download = runCatching {
            manager.downloadIndex.getDownload(downloadId(key))
        }.getOrNull()

        if (download == null) {
            _states.update { current ->
                val updated =
                    current + (
                        key to VideoDownloadState(
                            status = VideoDownloadStatus.NotDownloaded
                        )
                        )

                recalculateDownloadedUrls(updated)
                updated
            }

            ensureProgressLoop()
            return
        }

        updateState(download, key)
    }

    fun download(url: String, title: String? = null) {
        val key = downloadKey(url)
        val existingDownload = runCatching {
            manager.downloadIndex.getDownload(downloadId(key))
        }.getOrNull()

        if (
            existingDownload?.state == Download.STATE_DOWNLOADING ||
            existingDownload?.state == Download.STATE_QUEUED
        ) {
            return
        }

        if (existingDownload?.state == Download.STATE_COMPLETED) {
            updateState(existingDownload, key)
            return
        }

        _states.update {
            it + (
                key to VideoDownloadState(
                    status = VideoDownloadStatus.Downloading,
                    progressPercent = 0f,
                    title = title
                )
                )
        }

        ensureProgressLoop()
        prepareDownload(url, title)
    }

    private fun prepareDownload(url: String, title: String?) {
        val key = downloadKey(url)
        val mediaItem = MediaItem.fromUri(url.toUri())

        val dataSourceFactory =
            DownloadUtil.getDownloadDataSourceFactory(applicationContext)

        val downloadHelper =
            DownloadHelper.Factory()
                .setDataSourceFactory(dataSourceFactory)
                .create(mediaItem)

        downloadHelper.prepare(
            object : DownloadHelper.Callback {

                override fun onPrepared(
                    helper: DownloadHelper,
                    tracksInfoAvailable: Boolean
                ) {
                    try {
                        val requestData = encodeMetadata(key, title)

                        val request = helper.getDownloadRequest(
                            downloadId(key),
                            requestData
                        )

                        DownloadService.sendAddDownload(
                            applicationContext,
                            VideoDownloadService::class.java,
                            request,
                            true
                        )

                        DownloadService.sendResumeDownloads(
                            applicationContext,
                            VideoDownloadService::class.java,
                            false
                        )
                    } catch (_: Exception) {
                        _states.update {
                            it + (
                                key to VideoDownloadState(
                                    status = VideoDownloadStatus.Failed,
                                    title = title
                                )
                                )
                        }
                        ensureProgressLoop()
                    } finally {
                        helper.release()
                    }
                }

                override fun onPrepareError(
                    helper: DownloadHelper,
                    e: java.io.IOException
                ) {
                    _states.update {
                        it + (
                            key to VideoDownloadState(
                                status = VideoDownloadStatus.Failed,
                                title = title
                            )
                            )
                    }
                    ensureProgressLoop()

                    helper.release()
                }
            }
        )
    }

    fun remove(url: String) {
        val key = downloadKey(url)
        DownloadService.sendRemoveDownload(
            applicationContext,
            VideoDownloadService::class.java,
            downloadId(key),
            false
        )

        _states.update {
            val updated = it - key
            recalculateDownloadedUrls(updated)
            updated
        }

        ensureProgressLoop()
    }

    fun playbackUrl(url: String): String = url


    fun downloadedMediaItem(url: String): MediaItem? {
        val download = runCatching {
            manager.downloadIndex.getDownload(downloadId(url))
        }.getOrNull() ?: return null

        if (download.state != Download.STATE_COMPLETED) {
            return null
        }

        return download.request.toMediaItem()
    }

    fun refreshDownloaded() {
        synchronizeWithDownloadIndex()
    }

    private fun refreshCurrentDownloads() {
        val downloads = runCatching {
            manager.currentDownloads
        }.getOrNull() ?: return

        for (download in downloads) {
            updateState(download)
        }
    }

    private fun updateState(
        download: Download,
        explicitUrl: String? = null
    ) {
        val url = downloadKey(explicitUrl ?: requestUrl(download.request))
        val title = requestTitle(download.request)

        val progress =
            normalizeProgress(
                download.percentDownloaded,
                download.state
            )

        val mappedStatus =
            toStatus(download.state)

        _states.update { current ->
            val updated =
                current + (
                    url to VideoDownloadState(
                        status = mappedStatus,
                        progressPercent = progress,
                        title = title
                    )
                    )

            recalculateDownloadedUrls(updated)
            updated
        }

        ensureProgressLoop()
    }

    private fun synchronizeWithDownloadIndex() {
        val downloads = runCatching {
            manager.downloadIndex.getDownloads()
        }.getOrNull() ?: return

        val refreshed =
            linkedMapOf<String, VideoDownloadState>()

        downloads.useCursor { download ->
            val url = downloadKey(requestUrl(download.request))
            val title = requestTitle(download.request)

            refreshed[url] =
                VideoDownloadState(
                    status = toStatus(download.state),
                    progressPercent =
                        normalizeProgress(
                            download.percentDownloaded,
                            download.state
                        ),
                    title = title
                )
        }

        _states.value = refreshed
        recalculateDownloadedUrls(refreshed)
        ensureProgressLoop()
    }

    private fun requestUrl(request: DownloadRequest): String {
        val raw =
            if (request.data.isNotEmpty()) {
                decodeMetadata(request.data).url.ifBlank {
                    request.uri.toString()
                }
            } else {
                request.uri.toString()
            }

        return downloadKey(raw)
    }

    private fun requestTitle(request: DownloadRequest): String? {
        return if (request.data.isNotEmpty()) {
            decodeMetadata(request.data).title
        } else {
            null
        }
    }

    private fun encodeMetadata(url: String, title: String?): ByteArray {
        val json = JSONObject()
            .put("url", url)
            .put("title", title.orEmpty())

        return json.toString().toByteArray(Charsets.UTF_8)
    }

    private fun decodeMetadata(data: ByteArray): DownloadMetadata {
        return try {
            val json = JSONObject(data.decodeToString())
            DownloadMetadata(
                url = json.optString("url")
                    .ifBlank { data.decodeToString() },
                title = json.optString("title")
                    .takeIf { it.isNotBlank() }
            )
        } catch (_: Exception) {
            // Old format: data was just the URL
            DownloadMetadata(
                url = data.decodeToString(),
                title = null
            )
        }
    }

    private data class DownloadMetadata(
        val url: String,
        val title: String?
    )

    private fun normalizeProgress(
        progress: Float,
        state: Int
    ): Float? {
        return when {
            state == Download.STATE_COMPLETED -> 100f

            progress in 0f..100f -> progress

            state == Download.STATE_DOWNLOADING ||
                state == Download.STATE_QUEUED -> 0f

            else -> null
        }
    }

    private fun toStatus(downloadState: Int): VideoDownloadStatus {
        return when (downloadState) {
            Download.STATE_COMPLETED ->
                VideoDownloadStatus.Downloaded

            Download.STATE_FAILED ->
                VideoDownloadStatus.Failed

            Download.STATE_DOWNLOADING,
            Download.STATE_QUEUED,
            Download.STATE_RESTARTING ->
                VideoDownloadStatus.Downloading

            Download.STATE_STOPPED ->
                VideoDownloadStatus.Downloading

            Download.STATE_REMOVING ->
                VideoDownloadStatus.NotDownloaded

            else ->
                VideoDownloadStatus.NotDownloaded
        }
    }

    private fun downloadId(url: String): String {
        val bytes =
            MessageDigest
                .getInstance("SHA-256")
                .digest(downloadKey(url).encodeToByteArray())

        return buildString(bytes.size * 2) {
            for (byte in bytes) {
                append("%02x".format(byte))
            }
        }
    }

    private fun ensureProgressLoop() {
        val hasActive =
            _states.value.values.any {
                it.status == VideoDownloadStatus.Downloading
            }

        if (hasActive && !progressLoopScheduled) {
            progressLoopScheduled = true
            progressHandler.postDelayed(progressRunnable, PROGRESS_UPDATE_INTERVAL_MS)
        } else if (!hasActive && progressLoopScheduled) {
            progressLoopScheduled = false
            progressHandler.removeCallbacks(progressRunnable)
        }
    }

    private fun recalculateDownloadedUrls(
        stateMap: Map<String, VideoDownloadState>
    ) {
        _downloadedUrls.value =
            stateMap
                .asSequence()
                .filter { (_, state) ->
                    state.status == VideoDownloadStatus.Downloaded
                }
                .map { (url, _) -> url }
                .sorted()
                .toList()
    }

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }
}

@OptIn(UnstableApi::class)
private inline fun DownloadCursor.useCursor(
    block: (Download) -> Unit
) {
    use {
        while (moveToNext()) {
            block(download)
        }
    }
}