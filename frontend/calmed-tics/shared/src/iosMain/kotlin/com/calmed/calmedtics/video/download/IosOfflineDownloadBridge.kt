package com.calmed.calmedtics.video.download

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("IosOfflineDownloadListener")
interface IosOfflineDownloadListener {
	fun onDownloadState(
		playbackId: String,
		status: String,
		progress: Double,
		title: String?,
	)

	fun onDownloadRemoved(playbackId: String)
}

/** Implemented in Swift on top of `MuxOfflineAccessManager` from Mux Player Swift. */
@OptIn(ExperimentalObjCName::class)
@ObjCName("IosOfflineDownloadBridge")
interface IosOfflineDownloadBridge {
	fun startObserving(listener: IosOfflineDownloadListener)
	fun resumePendingDownloads()
	fun startDownload(
		playbackId: String,
		token: String?,
		title: String?,
		maxResolution: String,
	)

	fun removeDownload(playbackId: String)
	fun refresh()
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("IosOfflineDownloadRegistry")
object IosOfflineDownloadRegistry {
	var bridge: IosOfflineDownloadBridge? = null
}
