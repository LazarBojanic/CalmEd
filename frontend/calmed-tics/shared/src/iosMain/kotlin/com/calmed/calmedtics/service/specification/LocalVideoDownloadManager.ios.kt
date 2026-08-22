package com.calmed.calmedtics.service.specification

import kotlinx.cinterop.ExperimentalForeignApi
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
private const val OFFLINE_TITLE_PREFIX = "offline_video_title::"
private const val DOWNLOAD_SESSION_ID = "com.calmed.calmedtics.video.download"

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
            val remoteKey = pendingTaskIds.remove(assetDownloadTask.taskIdentifier.toLong())
                ?: assetDownloadTask.URLAsset.URL?.absoluteString
                ?: return
            val key = downloadKey(remoteKey)

            defaults.setObject(didFinishDownloadingToURL.absoluteString, forKey = keyFor(key))
            val title = defaults.stringForKey(titleKeyFor(key))
            _states.update {
                it + (key to VideoDownloadState(VideoDownloadStatus.Downloaded, progressPercent = 100f, title = title))
            }
            refreshDownloaded()
        }

        override fun URLSession(
            session: NSURLSession,
            task: NSURLSessionTask,
            didCompleteWithError: NSError?
        ) {
            val remoteKey = pendingTaskIds.remove(task.taskIdentifier.toLong()) ?: return
            if (didCompleteWithError != null) {
                val key = downloadKey(remoteKey)
                _states.update {
                    it + (key to VideoDownloadState(VideoDownloadStatus.Failed))
                }
            }
        }
    }

    private val session: AVAssetDownloadURLSession by lazy {
        val configuration = NSURLSessionConfiguration.backgroundSessionConfigurationWithIdentifier(
            identifier = DOWNLOAD_SESSION_ID
        )
        AVAssetDownloadURLSession.sessionWithConfiguration(
            configuration = configuration,
            assetDownloadDelegate = delegate,
            delegateQueue = NSOperationQueue.mainQueue()
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
        val key = downloadKey(url)
        val local = localFileUrl(key)
        val title = defaults.stringForKey(titleKeyFor(key))
        val state = if (local != null) {
            VideoDownloadState(VideoDownloadStatus.Downloaded, progressPercent = 100f, title = title)
        } else {
            VideoDownloadState(VideoDownloadStatus.NotDownloaded)
        }
        _states.update { it + (key to state) }
    }

    actual override fun refreshDownloaded() {
        val allKeys = (defaults.dictionaryRepresentation() as Map<Any?, *>).keys
        val urls = allKeys.mapNotNull { key: Any? ->
            (key as? String)
                ?.takeIf { it.startsWith(OFFLINE_URL_PREFIX) }
                ?.removePrefix(OFFLINE_URL_PREFIX)
        }.filter { url: String ->
            localFileUrl(url) != null
        }.distinct().sorted()

        _downloadedUrls.value = urls
    }

    actual override fun download(url: String, title: String?) {
        val key = downloadKey(url)
        val remote = NSURL(string = url) ?: run {
            _states.update { it + (key to VideoDownloadState(VideoDownloadStatus.Failed)) }
            return
        }
        if (localFileUrl(key) != null) {
            _states.update {
                it + (key to VideoDownloadState(VideoDownloadStatus.Downloaded, progressPercent = 100f, title = title))
            }
            return
        }

        title?.let { defaults.setObject(it, forKey = titleKeyFor(key)) }

        val asset = AVURLAsset(remote, options = null)
        val task = session.assetDownloadTaskWithURLAsset(
            asset,
            "Offline Video",
            null,
            null
        )

        if (task == null) {
            _states.update { it + (key to VideoDownloadState(VideoDownloadStatus.Failed)) }
            return
        }

        pendingTaskIds[task.taskIdentifier.toLong()] = key
        _states.update { it + (key to VideoDownloadState(VideoDownloadStatus.Downloading, progressPercent = 0f, title = title)) }
        task.resume()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun remove(url: String) {
        val key = downloadKey(url)
        localFileUrl(key)?.let { localUrl ->
            NSFileManager.defaultManager.removeItemAtURL(localUrl, error = null)
        }
        defaults.removeObjectForKey(keyFor(key))
        defaults.removeObjectForKey(titleKeyFor(key))
        _states.update { it - key }
        refreshDownloaded()
    }

    fun playbackUrl(url: String): String {
        return localFileUrl(url)?.absoluteString ?: url
    }

    private fun keyFor(url: String): String = OFFLINE_URL_PREFIX + downloadKey(url)

    private fun titleKeyFor(url: String): String = OFFLINE_TITLE_PREFIX + downloadKey(url)

    private fun localFileUrl(url: String): NSURL? {
        val stored = defaults.stringForKey(keyFor(url)) ?: return null
        val local = NSURL(string = stored) ?: return null
        val path = local.path ?: return null
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) local else null
    }
}
