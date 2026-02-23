package com.calmed.calmedfrontendtourettes.video.download

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadCursor
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.calmed.calmedfrontendtourettes.service.specification.VideoDownloadState
import com.calmed.calmedfrontendtourettes.service.specification.VideoDownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.security.MessageDigest

@OptIn(UnstableApi::class)
class AndroidVideoDownloadManager(context: Context) {
    private val applicationContext = context.applicationContext
    private val manager: DownloadManager = DownloadUtil.getDownloadManager(applicationContext)

    private val _states = MutableStateFlow<Map<String, VideoDownloadState>>(emptyMap())
    val states: StateFlow<Map<String, VideoDownloadState>> = _states

    private val _downloadedUrls = MutableStateFlow<List<String>>(emptyList())
    val downloadedUrls: StateFlow<List<String>> = _downloadedUrls

    init {
        manager.resumeDownloads()
        synchronizeWithDownloadIndex()

        manager.addListener(object : DownloadManager.Listener {
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

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                val url = requestUrl(download.request)
                _states.update {
                    val updated = it - url
                    recalculateDownloadedUrls(updated)
                    updated
                }
            }
        })
    }

    fun refresh(url: String) {
        val download = runCatching { manager.downloadIndex.getDownload(downloadId(url)) }
            .getOrNull()
        if (download == null) {
            _states.update { current ->
                val updated = current + (url to VideoDownloadState(VideoDownloadStatus.NotDownloaded))
                recalculateDownloadedUrls(updated)
                updated
            }
            return
        }
        updateState(download, url)
    }

    fun download(url: String) {
        val request = DownloadRequest.Builder(downloadId(url), Uri.parse(url))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .setData(url.encodeToByteArray())
            .build()

        _states.update {
            it + (url to VideoDownloadState(VideoDownloadStatus.Downloading, progressPercent = 0f))
        }

        DownloadService.sendAddDownload(
            applicationContext,
            VideoDownloadService::class.java,
            request,
            true
        )
        DownloadService.sendResumeDownloads(applicationContext, VideoDownloadService::class.java, false)
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

    fun refreshDownloaded() {
        synchronizeWithDownloadIndex()
    }

    private fun updateState(download: Download, explicitUrl: String? = null) {
        val url = explicitUrl ?: requestUrl(download.request)
        val progress = normalizeProgress(download.percentDownloaded, download.state)
        val mappedStatus = toStatus(download.state)

        _states.update { current ->
            val updated = current + (url to VideoDownloadState(mappedStatus, progress))
            recalculateDownloadedUrls(updated)
            updated
        }
    }

    private fun synchronizeWithDownloadIndex() {
        val downloads = runCatching {
            manager.downloadIndex.getDownloads()
        }.getOrNull() ?: return

        val refreshed = linkedMapOf<String, VideoDownloadState>()
        downloads.useCursor { download ->
            val url = requestUrl(download.request)
            refreshed[url] = VideoDownloadState(
                status = toStatus(download.state),
                progressPercent = normalizeProgress(download.percentDownloaded, download.state)
            )
        }
        _states.value = refreshed
        recalculateDownloadedUrls(refreshed)
    }

    private fun requestUrl(request: DownloadRequest): String {
        return if (request.data.isNotEmpty()) {
            request.data.decodeToString()
        } else {
            request.uri.toString()
        }
    }

    private fun normalizeProgress(progress: Float, state: Int): Float? {
        return when {
            state == Download.STATE_COMPLETED -> 100f
            progress in 0f..100f -> progress
            state == Download.STATE_DOWNLOADING || state == Download.STATE_QUEUED -> 0f
            else -> null
        }
    }

    private fun toStatus(downloadState: Int): VideoDownloadStatus {
        return when (downloadState) {
            Download.STATE_COMPLETED -> VideoDownloadStatus.Downloaded
            Download.STATE_FAILED -> VideoDownloadStatus.Failed
            Download.STATE_DOWNLOADING,
            Download.STATE_QUEUED,
            Download.STATE_RESTARTING,
            Download.STATE_STOPPED -> VideoDownloadStatus.Downloading
            Download.STATE_REMOVING -> VideoDownloadStatus.NotDownloaded
            else -> VideoDownloadStatus.NotDownloaded
        }
    }

    private fun downloadId(url: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(url.encodeToByteArray())
        return buildString(bytes.size * 2) {
            for (byte in bytes) append("%02x".format(byte))
        }
    }

    private fun recalculateDownloadedUrls(stateMap: Map<String, VideoDownloadState>) {
        _downloadedUrls.value = stateMap
            .asSequence()
            .filter { (_, state) -> state.status == VideoDownloadStatus.Downloaded }
            .map { (url, _) -> url }
            .sorted()
            .toList()
    }
}

private inline fun DownloadCursor.useCursor(block: (Download) -> Unit) {
    try {
        while (moveToNext()) {
            block(getDownload())
        }
    } finally {
        close()
    }
}
