package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.calmed.calmedtics.video.VideoQuality

data class VideoItem(
	val playbackId: String,
	val playbackToken: String? = null,
	val title: String? = null,
)

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
	onFullscreenToggle: (Boolean) -> Unit = {},
	onIndexChanged: (Int) -> Unit = {},
)
