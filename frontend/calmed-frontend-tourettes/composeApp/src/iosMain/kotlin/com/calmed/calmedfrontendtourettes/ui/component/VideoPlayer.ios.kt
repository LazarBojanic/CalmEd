package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
	hlsUrl: String,
	modifier: Modifier
) {
	val player = remember { AVPlayer() }

	val controller = remember {
		AVPlayerViewController().apply {
			this.player = player
			this.showsPlaybackControls = true
			this.allowsPictureInPicturePlayback = true
			this.canStartPictureInPictureAutomaticallyFromInline = true
		}
	}

	DisposableEffect(Unit) {
		onDispose {
			player.pause()
			player.replaceCurrentItemWithPlayerItem(null)
			controller.player = null
		}
	}

	LaunchedEffect(hlsUrl) {
		val nsUrl = NSURL(string = hlsUrl)
		if (nsUrl != null) {
			val item = AVPlayerItem(uRL = nsUrl)
			player.replaceCurrentItemWithPlayerItem(item)
			player.play()
		} else {
			player.pause()
			player.replaceCurrentItemWithPlayerItem(null)
		}
	}
	UIKitView(
		modifier = modifier,
		factory = {
			val container = UIView()
			val playerView = controller.view ?: UIView()
			playerView.translatesAutoresizingMaskIntoConstraints = true
			playerView.autoresizingMask =
				UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
			container.addSubview(playerView)

			container
		},
		update = { _container ->
			// no-op: autoresizing mask will keep player view sized to container.
			// If you prefer Auto Layout constraints, create and activate them here instead of changing frame.
		},
		properties = UIKitInteropProperties(
			isInteractive = true,
			isNativeAccessibilityEnabled = true
		)
	)
}
