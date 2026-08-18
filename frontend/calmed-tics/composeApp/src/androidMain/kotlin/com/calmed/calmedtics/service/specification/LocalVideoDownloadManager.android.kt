package com.calmed.calmedtics.service.specification

import com.calmed.calmedtics.di.appContext
import com.calmed.calmedtics.video.download.AndroidVideoDownloadManager
import kotlinx.coroutines.flow.StateFlow

actual object LocalVideoDownloadManager : IVideoDownloadManager {

    private val delegate by lazy {
        AndroidVideoDownloadManager(appContext)
    }

    actual override val states: StateFlow<Map<String, VideoDownloadState>>
        get() = delegate.states

    actual override val downloadedUrls: StateFlow<List<String>>
        get() = delegate.downloadedUrls

    actual override fun refresh(url: String) {
        delegate.refresh(url)
    }

    actual override fun refreshDownloaded() {
        delegate.refreshDownloaded()
    }

    actual override fun download(url: String, title: String?) {
        delegate.download(url, title)
    }

    actual override fun remove(url: String) {
        delegate.remove(url)
    }

    fun downloadedMediaItem(url: String) =
        delegate.downloadedMediaItem(url)
}