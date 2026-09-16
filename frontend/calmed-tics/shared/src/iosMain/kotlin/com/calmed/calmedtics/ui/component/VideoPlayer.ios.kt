package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.calmed.calmedtics.video.VideoQuality
import com.calmed.calmedtics.video.player.IosVideoPlayerListener
import com.calmed.calmedtics.video.player.IosVideoPlayerRegistry
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
	items: List<VideoItem>,
	startIndex: Int,
	modifier: Modifier,
	quality: VideoQuality,
	muted: Boolean,
	autoPlay: Boolean,
	allowFullscreen: Boolean,
	isFullscreen: Boolean,
	onFullscreenToggle: (Boolean) -> Unit,
	onIndexChanged: (Int) -> Unit,
	onControlsVisibilityChanged: (Boolean) -> Unit,
	onIsPlayingChanged: (Boolean) -> Unit,
) {
	val bridge = IosVideoPlayerRegistry.bridge
	if (bridge == null || items.isEmpty()) {
		Box(modifier.fillMaxSize())
		return
	}

	val currentOnIndexChanged by rememberUpdatedState(onIndexChanged)
	val currentOnControlsVisibilityChanged by rememberUpdatedState(onControlsVisibilityChanged)
	val currentOnIsPlayingChanged by rememberUpdatedState(onIsPlayingChanged)
	val listener = remember {
		object : IosVideoPlayerListener {
			override fun onIndexChanged(index: Int) {
				currentOnIndexChanged(index)
			}

			override fun onControlsVisibilityChanged(visible: Boolean) {
				currentOnControlsVisibilityChanged(visible)
			}

			override fun onIsPlayingChanged(isPlaying: Boolean) {
				currentOnIsPlayingChanged(isPlaying)
			}
		}
	}

	UIKitViewController(
		factory = {
			bridge.createController(
				items = items,
				startIndex = startIndex,
				quality = quality,
				muted = muted,
				listener = listener,
			)
		},
		modifier = modifier,
		update = { controller ->
			bridge.update(
				controller = controller,
				items = items,
				startIndex = startIndex,
				quality = quality,
				muted = muted,
			)
		},
		onRelease = { controller -> bridge.releaseController(controller) },
		properties = UIKitInteropProperties(
			isInteractive = true,
			isNativeAccessibilityEnabled = true,
		),
	)
}
