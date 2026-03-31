package com.calmed.calmedtics.service.specification

import kotlinx.coroutines.flow.StateFlow

enum class VideoDownloadStatus {
    NotDownloaded,
    Downloading,
    Downloaded,
    Failed
}

data class VideoDownloadState(
    val status: VideoDownloadStatus,
    val progressPercent: Float? = null
)

interface IVideoDownloadManager {
    val states: StateFlow<Map<String, VideoDownloadState>>
    val downloadedUrls: StateFlow<List<String>>

    fun refresh(url: String)
    fun refreshDownloaded()
    fun download(url: String)
    fun remove(url: String)
    fun playbackUrl(url: String): String
}

expect object LocalVideoDownloadManager : IVideoDownloadManager {
    override val states: StateFlow<Map<String, VideoDownloadState>>
    override val downloadedUrls: StateFlow<List<String>>

    override fun refresh(url: String)
    override fun refreshDownloaded()
    override fun download(url: String)
    override fun remove(url: String)
    override fun playbackUrl(url: String): String
}

fun Map<String, VideoDownloadState>.stateFor(url: String): VideoDownloadState {
    return this[url] ?: VideoDownloadState(VideoDownloadStatus.NotDownloaded)
}
