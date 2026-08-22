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
    val progressPercent: Float? = null,
    val title: String? = null
)

interface IVideoDownloadManager {
    val states: StateFlow<Map<String, VideoDownloadState>>
    val downloadedUrls: StateFlow<List<String>>

    fun refresh(url: String)
    fun refreshDownloaded()
    fun download(url: String, title: String? = null)
    fun remove(url: String)
}

expect object LocalVideoDownloadManager : IVideoDownloadManager {
    override val states: StateFlow<Map<String, VideoDownloadState>>
    override val downloadedUrls: StateFlow<List<String>>

    override fun refresh(url: String)
    override fun refreshDownloaded()
    override fun download(url: String, title: String?)
    override fun remove(url: String)
}

/**
 * Returns the stable identity of a video URL.
 *
 * Signed Mux URLs carry a short-lived `?token=...` query parameter that changes
 * on every backend response. Using the tokenized URL as a download identity
 * would make a downloaded video "forget" it is downloaded after any refetch, so
 * the volatile query string is stripped here.
 */
fun downloadKey(url: String): String = url.substringBefore('?')

fun Map<String, VideoDownloadState>.stateFor(url: String): VideoDownloadState {
    return this[downloadKey(url)] ?: VideoDownloadState(VideoDownloadStatus.NotDownloaded)
}