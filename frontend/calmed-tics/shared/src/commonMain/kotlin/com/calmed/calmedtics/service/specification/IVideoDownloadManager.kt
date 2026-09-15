package com.calmed.calmedtics.service.specification

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class VideoDownloadStatus {
    NotDownloaded,
    Downloading,
    Downloaded,
    Failed
}

data class VideoDownloadState(
    val status: VideoDownloadStatus,
    val progressPercent: Float? = null,
    val title: String? = null
)

enum class DownloadEventType {
    Completed,
    Failed
}

data class DownloadEvent(
    val type: DownloadEventType,
    val title: String?
)

interface IVideoDownloadManager {
    val states: StateFlow<Map<String, VideoDownloadState>>
    val downloadedUrls: StateFlow<List<String>>
    val events: SharedFlow<DownloadEvent>

    fun refresh(url: String)
    fun refreshDownloaded()
    fun download(url: String, title: String? = null)
    fun remove(url: String)
}


fun downloadKey(url: String): String = url.substringBefore('?')

fun Map<String, VideoDownloadState>.stateFor(url: String): VideoDownloadState {
    return this[downloadKey(url)] ?: VideoDownloadState(VideoDownloadStatus.NotDownloaded)
}