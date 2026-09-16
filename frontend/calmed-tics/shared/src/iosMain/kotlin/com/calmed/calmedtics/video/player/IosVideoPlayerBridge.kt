package com.calmed.calmedtics.video.player

import com.calmed.calmedtics.ui.component.VideoItem
import com.calmed.calmedtics.video.VideoQuality
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import platform.UIKit.UIViewController

/** Implemented in Swift on top of `MuxPlayerSwift`. */
@OptIn(ExperimentalObjCName::class)
@ObjCName("IosVideoPlayerBridge")
interface IosVideoPlayerBridge {
	fun createController(
		items: List<VideoItem>,
		startIndex: Int,
		quality: VideoQuality,
		muted: Boolean,
		listener: IosVideoPlayerListener,
	): UIViewController

	fun update(
		controller: UIViewController,
		items: List<VideoItem>,
		startIndex: Int,
		quality: VideoQuality,
		muted: Boolean,
	)

	fun releaseController(controller: UIViewController)
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("IosVideoPlayerListener")
interface IosVideoPlayerListener {
	fun onIndexChanged(index: Int)
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("IosVideoPlayerRegistry")
object IosVideoPlayerRegistry {
	var bridge: IosVideoPlayerBridge? = null
}
