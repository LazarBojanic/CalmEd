package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.calmed.calmedtics.video.VideoQuality

data class VideoItem(
	val playbackId: String,
	val token480: String? = null,
	val token720: String? = null,
	val token1080: String? = null,
	val title: String? = null,
) {
	fun tokenFor(quality: VideoQuality): String? = when (quality) {
		VideoQuality.R480 -> token480
		VideoQuality.R720 -> token720
		VideoQuality.R1080 -> token1080
	}
}

@Composable
expect fun VideoPlayer(
	items: List<VideoItem>,
	startIndex: Int,
	modifier: Modifier = Modifier,
	quality: VideoQuality = VideoQuality.R720,
	muted: Boolean = false,
	autoPlay: Boolean = true,
	allowFullscreen: Boolean = true,
	isFullscreen: Boolean = false,
	isImmersive: Boolean = false,
	onFullscreenToggle: (Boolean) -> Unit = {},
	onIndexChanged: (Int) -> Unit = {},
	onControlsVisibilityChanged: (Boolean) -> Unit = {},
	onIsPlayingChanged: (Boolean) -> Unit = {},
)
