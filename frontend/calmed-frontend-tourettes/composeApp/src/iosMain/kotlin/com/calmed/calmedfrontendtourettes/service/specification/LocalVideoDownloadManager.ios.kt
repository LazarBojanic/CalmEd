package com.calmed.calmedfrontendtourettes.service.specification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVAssetDownloadDelegateProtocol
import platform.AVFoundation.AVAssetDownloadTask
import platform.AVFoundation.AVAssetDownloadURLSession
import platform.AVFoundation.AVURLAsset
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSUserDefaults
import platform.darwin.NSObject

private const val OFFLINE_URL_PREFIX = "offline_video_url::"
private const val DOWNLOAD_SESSION_ID = "com.calmed.calmedfrontendtourettes.video.download"

actual object LocalVideoDownloadManager : IVideoDownloadManager {
    private val _states = MutableStateFlow<Map<String, VideoDownloadState>>(emptyMap())
    private val _downloadedUrls = MutableStateFlow<List<String>>(emptyList())

    private val defaults = NSUserDefaults.standardUserDefaults
    private val pendingTaskIds = mutableMapOf<Long, String>()

    private val delegate = object : NSObject(), AVAssetDownloadDelegateProtocol {
        override fun URLSession(
            session: NSURLSession,
            assetDownloadTask: AVAssetDownloadTask,
            didFinishDownloadingToURL: NSURL
        ) {
            val remoteUrl = pendingTaskIds.remove(assetDownloadTask.taskIdentifier.toLong())
                ?: assetDownloadTask.URLAsset.URL?.absoluteString
                ?: return

            defaults.setObject(didFinishDownloadingToURL.absoluteString, forKey = keyFor(remoteUrl))
            _states.update {
                it + (remoteUrl to VideoDownloadState(VideoDownloadStatus.Downloaded, progressPercent = 100f))
            }
            refreshDownloaded()
        }

        override fun URLSession(
            session: NSURLSession,
            task: NSURLSessionTask,
            didCompleteWithError: NSError?
        ) {
            val remoteUrl = pendingTaskIds.remove(task.taskIdentifier.toLong()) ?: return
            if (didCompleteWithError != null) {
                _states.update {
                    it + (remoteUrl to VideoDownloadState(VideoDownloadStatus.Failed))
                }
            }
        }
    }

    private val session: AVAssetDownloadURLSession by lazy {
        val configuration = NSURLSessionConfiguration.backgroundSessionConfigurationWithIdentifier(
            identifier = DOWNLOAD_SESSION_ID
        )
        AVAssetDownloadURLSession.URLSessionWithConfiguration(
            configuration,
            delegate,
            NSOperationQueue.mainQueue()
        )
    }

    actual override val states: StateFlow<Map<String, VideoDownloadState>>
        get() = _states

    actual override val downloadedUrls: StateFlow<List<String>>
        get() = _downloadedUrls

    init {
        refreshDownloaded()
    }

    actual override fun refresh(url: String) {
        val local = localFileUrl(url)
        val state = if (local != null) {
            VideoDownloadState(VideoDownloadStatus.Downloaded, progressPercent = 100f)
        } else {
            VideoDownloadState(VideoDownloadStatus.NotDownloaded)
        }
        _states.update { it + (url to state) }
    }

    actual override fun refreshDownloaded() {
        val allKeys = defaults.dictionaryRepresentation().allKeys
        val urls = allKeys.mapNotNull { key ->
            (key as? String)
                ?.takeIf { it.startsWith(OFFLINE_URL_PREFIX) }
                ?.removePrefix(OFFLINE_URL_PREFIX)
        }.filter { url ->
            localFileUrl(url) != null
        }.distinct().sorted()

        _downloadedUrls.value = urls
    }

    actual override fun download(url: String) {
        val remote = NSURL(string = url) ?: run {
            _states.update { it + (url to VideoDownloadState(VideoDownloadStatus.Failed)) }
            return
        }
        if (localFileUrl(url) != null) {
            _states.update {
                it + (url to VideoDownloadState(VideoDownloadStatus.Downloaded, progressPercent = 100f))
            }
            return
        }

        val asset = AVURLAsset.URLAssetWithURL(URL = remote, options = null)
        val task = session.assetDownloadTaskWithURLAsset(
            asset,
            "Offline Video",
            null,
            null
        )

        if (task == null) {
            _states.update { it + (url to VideoDownloadState(VideoDownloadStatus.Failed)) }
            return
        }

        pendingTaskIds[task.taskIdentifier.toLong()] = url
        _states.update { it + (url to VideoDownloadState(VideoDownloadStatus.Downloading, progressPercent = 0f)) }
        task.resume()
    }

    actual override fun remove(url: String) {
        localFileUrl(url)?.let { localUrl ->
            NSFileManager.defaultManager.removeItemAtURL(localUrl, error = null)
        }
        defaults.removeObjectForKey(keyFor(url))
        _states.update { it - url }
        refreshDownloaded()
    }

    actual override fun playbackUrl(url: String): String {
        return localFileUrl(url)?.absoluteString ?: url
    }

    private fun keyFor(url: String): String = OFFLINE_URL_PREFIX + url

    private fun localFileUrl(url: String): NSURL? {
        val stored = defaults.stringForKey(keyFor(url)) ?: return null
        val local = NSURL(string = stored) ?: return null
        val path = local.path ?: return null
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) local else null
    }
}
