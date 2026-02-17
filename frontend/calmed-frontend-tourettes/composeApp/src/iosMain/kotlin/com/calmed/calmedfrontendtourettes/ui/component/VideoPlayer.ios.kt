package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.playbackBufferEmpty
import platform.AVFoundation.playbackLikelyToKeepUp
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSLayoutRelationEqual
import platform.UIKit.NSLayoutAttributeBottom
import platform.UIKit.NSLayoutAttributeLeading
import platform.UIKit.NSLayoutAttributeTop
import platform.UIKit.NSLayoutAttributeTrailing
import platform.UIKit.UIView
import platform.darwin.NSObjectProtocol

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
	hlsUrl: String,
	modifier: Modifier
) {
	var isBuffering by remember { mutableStateOf(false) }
	var isPlaying by remember { mutableStateOf(false) }
	var hasError by remember { mutableStateOf(false) }

	val player = remember { AVPlayer() }
	val controller = remember {
		AVPlayerViewController().apply {
			this.player = player
			this.showsPlaybackControls = true
			this.allowsPictureInPicturePlayback = true
			this.canStartPictureInPictureAutomaticallyFromInline = true
		}
	}
	val notificationTokens = remember { mutableStateListOf<NSObjectProtocol>() }
	LaunchedEffect(Unit) {
		val session = AVAudioSession.sharedInstance()
		session.setCategory(AVAudioSessionCategoryPlayback, error = null)
		session.setActive(true, error = null)
	}
	LaunchedEffect(hlsUrl) {
		notificationTokens.forEach { NSNotificationCenter.defaultCenter.removeObserver(it) }
		notificationTokens.clear()

		val nsUrl = NSURL(string = hlsUrl)
		if (nsUrl != null) {
			val item = AVPlayerItem(uRL = nsUrl)
			player.replaceCurrentItemWithPlayerItem(item)
			player.play()
			isPlaying = true
			hasError = false

			val endToken = NSNotificationCenter.defaultCenter.addObserverForName(
				name = AVPlayerItemDidPlayToEndTimeNotification,
				`object` = item,
				queue = null
			) {
				isPlaying = false
			}
			notificationTokens.add(endToken)

			try {
				while (true) {
					val current = player.currentItem
					if (current == null || current !== item) break

					when (item.status) {
						platform.AVFoundation.AVPlayerItemStatusReadyToPlay -> {
							isBuffering = false
							hasError = false
						}
						platform.AVFoundation.AVPlayerItemStatusFailed -> {
							isBuffering = false
							hasError = true
							isPlaying = false
						}
						else -> {
							isBuffering = item.playbackBufferEmpty || !item.playbackLikelyToKeepUp
						}
					}
					delay(250)
				}
			} catch (e: Throwable) {
				isPlaying = false
			}
		} else {
			player.pause()
			player.replaceCurrentItemWithPlayerItem(null)
			isPlaying = false
			hasError = true
		}
	}

	DisposableEffect(Unit) {
		onDispose {
			player.pause()
			player.replaceCurrentItemWithPlayerItem(null)
			controller.player = null
			notificationTokens.forEach { NSNotificationCenter.defaultCenter.removeObserver(it) }
			notificationTokens.clear()
		}
	}

	UIKitView(
		modifier = modifier,
		factory = {
			val container = UIView()

			val playerView = controller.view
			playerView.translatesAutoresizingMaskIntoConstraints = false

			container.addSubview(playerView)

			val leading = NSLayoutConstraint.constraintWithItem(
				playerView,
				NSLayoutAttributeLeading,
				NSLayoutRelationEqual,
				container,
				NSLayoutAttributeLeading,
				1.0,
				0.0
			)
			val trailing = NSLayoutConstraint.constraintWithItem(
				playerView,
				NSLayoutAttributeTrailing,
				NSLayoutRelationEqual,
				container,
				NSLayoutAttributeTrailing,
				1.0,
				0.0
			)
			val top = NSLayoutConstraint.constraintWithItem(
				playerView,
				NSLayoutAttributeTop,
				NSLayoutRelationEqual,
				container,
				NSLayoutAttributeTop,
				1.0,
				0.0
			)
			val bottom = NSLayoutConstraint.constraintWithItem(
				playerView,
				NSLayoutAttributeBottom,
				NSLayoutRelationEqual,
				container,
				NSLayoutAttributeBottom,
				1.0,
				0.0
			)

			NSLayoutConstraint.activateConstraints(listOf(leading, trailing, top, bottom))

			container
		},
		update = { _container ->
		},
		properties = UIKitInteropProperties(
			isInteractive = true,
			isNativeAccessibilityEnabled = true
		)
	)
}
