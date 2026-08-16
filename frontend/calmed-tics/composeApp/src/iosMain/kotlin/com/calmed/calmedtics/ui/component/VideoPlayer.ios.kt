package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.stateFor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.muted
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.NSLayoutAttributeBottom
import platform.UIKit.NSLayoutAttributeLeading
import platform.UIKit.NSLayoutAttributeTop
import platform.UIKit.NSLayoutAttributeTrailing
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSLayoutRelationEqual
import platform.UIKit.UIView
import platform.darwin.NSEC_PER_SEC
import platform.darwin.NSObjectProtocol

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    useController: Boolean,
    onPositionChanged: ((Long) -> Unit)?,
    onDurationChanged: ((Long) -> Unit)?,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)?,
    onFullscreenToggle: ((Boolean) -> Unit)?,
    onPlaybackEnded: (() -> Unit)?,
    restartTrigger: Int
) {
    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
    val currentOnDurationChanged by rememberUpdatedState(onDurationChanged)
    val currentOnVideoOrientationChanged by rememberUpdatedState(onVideoOrientationChanged)
    val currentOnPlaybackEnded by rememberUpdatedState(onPlaybackEnded)

    val player = remember { AVPlayer() }
    val controller = remember {
        AVPlayerViewController().apply {
            this.player = player
            this.showsPlaybackControls = useController
            this.allowsPictureInPicturePlayback = true
            this.canStartPictureInPictureAutomaticallyFromInline = true
        }
    }
    val tokens = remember { mutableStateListOf<NSObjectProtocol>() }

    val states by LocalVideoDownloadManager.states.collectAsState()
    val downloadState = states.stateFor(hlsUrl)

    LaunchedEffect(Unit) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }

    LaunchedEffect(useController) {
        controller.showsPlaybackControls = useController
    }

    LaunchedEffect(isMuted) {
        player.muted = isMuted
    }

    LaunchedEffect(hlsUrl, downloadState.status) {
        LocalVideoDownloadManager.refresh(hlsUrl)

        tokens.forEach { NSNotificationCenter.defaultCenter.removeObserver(it) }
        tokens.clear()

        val playback = LocalVideoDownloadManager.playbackUrl(hlsUrl)
        val nsUrl = NSURL(string = playback)
        if (nsUrl == null) {
            player.pause()
            player.replaceCurrentItemWithPlayerItem(null)
            return@LaunchedEffect
        }

        val item = AVPlayerItem(uRL = nsUrl)
        player.replaceCurrentItemWithPlayerItem(item)

        val size = item.presentationSize
        size.useContents {
            if (width > 0.0 && height > 0.0) {
                val isPortrait = height >= width
                currentOnVideoOrientationChanged?.invoke(isPortrait)
            }
        }

        if (isPlaying) {
            player.play()
        }

        val endToken = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = null
        ) {
            currentOnPlaybackEnded?.invoke()
        }
        tokens.add(endToken)
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }

    LaunchedEffect(restartTrigger) {
        if (restartTrigger > 0) {
            player.seekToTime(CMTimeMakeWithSeconds(0.0, NSEC_PER_SEC.toInt()))
            player.play()
        }
    }

    DisposableEffect(player) {
        val interval = CMTimeMakeWithSeconds(0.5, NSEC_PER_SEC.toInt())
        val timeObserver = player.addPeriodicTimeObserverForInterval(interval, queue = null) { time ->
            val seconds = CMTimeGetSeconds(time)
            val posMs = (seconds * 1000.0).toLong()
            currentOnPositionChanged?.invoke(posMs)

            val currentItem = player.currentItem
            if (currentItem != null) {
                val durSec = CMTimeGetSeconds(currentItem.duration)
                if (!durSec.isNaN() && durSec > 0.0) {
                    currentOnDurationChanged?.invoke((durSec * 1000.0).toLong())
                }
                val presSize = currentItem.presentationSize
                presSize.useContents {
                    if (width > 0.0 && height > 0.0) {
                        val isPortrait = height >= width
                        currentOnVideoOrientationChanged?.invoke(isPortrait)
                    }
                }
            }
        }

        onDispose {
            player.removeTimeObserver(timeObserver)
            player.pause()
            player.replaceCurrentItemWithPlayerItem(null)
            controller.player = null
            tokens.forEach { NSNotificationCenter.defaultCenter.removeObserver(it) }
            tokens.clear()
        }
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
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
            update = {},
            properties = UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true
            )
        )
    }
}

@Composable
actual fun VideoPlayerWithState(
    hlsUrl: String,
    modifier: Modifier,
    isPlaying: Boolean,
    isMuted: Boolean,
    useController: Boolean,
    onPositionChanged: (Long) -> Unit,
    onDurationChanged: (Long) -> Unit,
    restartTrigger: Int,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)?,
    onFullscreenToggle: ((Boolean) -> Unit)?,
    onPlaybackEnded: (() -> Unit)?,
    isFullscreen: Boolean
) {
    VideoPlayer(
        hlsUrl = hlsUrl,
        modifier = modifier,
        isFullscreen = isFullscreen,
        isPlaying = isPlaying,
        isMuted = isMuted,
        useController = useController,
        onPositionChanged = onPositionChanged,
        onDurationChanged = onDurationChanged,
        onVideoOrientationChanged = onVideoOrientationChanged,
        onFullscreenToggle = onFullscreenToggle,
        onPlaybackEnded = onPlaybackEnded,
        restartTrigger = restartTrigger
    )
}
