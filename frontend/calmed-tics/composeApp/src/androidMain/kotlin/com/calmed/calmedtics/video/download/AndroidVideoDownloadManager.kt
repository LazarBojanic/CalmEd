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

    private val progressRunnable = object : Runnable {
        override fun run() {
            refreshCurrentDownloads()
            progressHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL_MS)
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
                }
            }
        )

        progressHandler.post(progressRunnable)
    }

    fun refresh(url: String) {
        val download = runCatching {
            manager.downloadIndex.getDownload(downloadId(url))
        }.getOrNull()

        if (download == null) {
            _states.update { current ->
                val updated =
                    current + (
                        url to VideoDownloadState(
                            status = VideoDownloadStatus.NotDownloaded
                        )
                        )

                recalculateDownloadedUrls(updated)
                updated
            }

            return
        }

        updateState(download, url)
    }

    fun download(url: String, title: String? = null) {
        val existingDownload = runCatching {
            manager.downloadIndex.getDownload(downloadId(url))
        }.getOrNull()

        if (
            existingDownload?.state == Download.STATE_DOWNLOADING ||
            existingDownload?.state == Download.STATE_QUEUED
        ) {
            return
        }

        if (existingDownload?.state == Download.STATE_COMPLETED) {
            updateState(existingDownload, url)
            return
        }

        _states.update {
            it + (
                url to VideoDownloadState(
                    status = VideoDownloadStatus.Downloading,
                    progressPercent = 0f,
                    title = title
                )
                )
        }

        prepareDownload(url, title)
    }

    private fun prepareDownload(url: String, title: String?) {
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
                        val requestData = encodeMetadata(url, title)

                        val request = helper.getDownloadRequest(
                            downloadId(url),
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
                                url to VideoDownloadState(
                                    status = VideoDownloadStatus.Failed,
                                    title = title
                                )
                                )
                        }
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
                            url to VideoDownloadState(
                                status = VideoDownloadStatus.Failed,
                                title = title
                            )
                            )
                    }

                    helper.release()
                }
            }
        )
    }

    fun remove(url: String) {
        DownloadService.sendRemoveDownload(
            applicationContext,
            VideoDownloadService::class.java,
            downloadId(url),
            false
        )

        _states.update {
            val updated = it - url
            recalculateDownloadedUrls(updated)
            updated
        }
    }

    fun playbackUrl(url: String): String = url

    /**
     * Returns the MediaItem associated with the completed download.
     *
     * This is important for HLS because DownloadRequest.toMediaItem()
     * carries the stream keys selected by DownloadHelper.
     */
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
        val url = explicitUrl ?: requestUrl(download.request)
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
    }

    private fun synchronizeWithDownloadIndex() {
        val downloads = runCatching {
            manager.downloadIndex.getDownloads()
        }.getOrNull() ?: return

        val refreshed =
            linkedMapOf<String, VideoDownloadState>()

        downloads.useCursor { download ->
            val url = requestUrl(download.request)
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
    }

    private fun requestUrl(request: DownloadRequest): String {
        return if (request.data.isNotEmpty()) {
            decodeMetadata(request.data).url.ifBlank {
                request.uri.toString()
            }
        } else {
            request.uri.toString()
        }
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
                .digest(url.encodeToByteArray())

        return buildString(bytes.size * 2) {
            for (byte in bytes) {
                append("%02x".format(byte))
            }
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