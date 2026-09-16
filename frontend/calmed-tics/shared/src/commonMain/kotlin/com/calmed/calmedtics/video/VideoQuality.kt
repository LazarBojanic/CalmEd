package com.calmed.calmedtics.video

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("VideoQuality")
enum class VideoQuality(
	val label: String,
	val maxResolution: String
) {
	R480("480p", "480p"),
	R720("720p", "720p"),
	R1080("1080p", "1080p");

	companion object {
		fun fromName(name: String?): VideoQuality =
			entries.firstOrNull { it.name == name } ?: R720
	}
}
