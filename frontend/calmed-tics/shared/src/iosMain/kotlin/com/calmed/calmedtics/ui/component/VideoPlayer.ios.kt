package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.darwin.NSEC_PER_SEC
import platform.darwin.NSObjectProtocol

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    title: String?,
    modifier: Modifier,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    useController: Boolean,
    playlist: List<VideoPlaylistItem>?,
    onPositionChanged: ((Long) -> Unit)?,
    onDurationChanged: ((Long) -> Unit)?,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)?,
    onFullscreenToggle: ((Boolean) -> Unit)?,
    onPlaybackEnded: (() -> Unit)?,
    onPlayPauseChange: ((Boolean) -> Unit)?,
    onPlaylistIndexChanged: ((Int) -> Unit)?,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    autoPlayNext: Boolean,
    repeatCurrentExercise: Boolean,
    startPositionMs: Long,
    restartTrigger: Int
) {
    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
    val currentOnDurationChanged by rememberUpdatedState(onDurationChanged)
    val currentOnVideoOrientationChanged by rememberUpdatedState(onVideoOrientationChanged)
    val currentOnPlaybackEnded by rememberUpdatedState(onPlaybackEnded)
    val currentOnPlayPauseChange by rememberUpdatedState(onPlayPauseChange)

    val player = remember { AVPlayer() }
    val controller = remember {
        AVPlayerViewController().apply {
            this.player = player
            this.showsPlaybackControls = useController
            this.allowsPictureInPicturePlayback = true
            this.canStartPictureInPictureAutomaticallyFromInline = true
        }
    }
    var endToken by remember { mutableStateOf<NSObjectProtocol?>(null) }

    /*
     * Tracks the last play state reported to the screen so we only fire
     * onPlayPauseChange on actual transitions (the periodic observer runs
     * several times per second).
     */
    var lastReportedPlaying by remember {
        mutableStateOf(
            player.timeControlStatus != AVPlayerTimeControlStatusPaused
        )
    }

    var hasSeekedToStart by remember { mutableStateOf(false) }

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

    LaunchedEffect(hlsUrl) {
        LocalVideoDownloadManager.refresh(hlsUrl)

        endToken?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endToken = null

        val playback = LocalVideoDownloadManager.playbackUrl(hlsUrl)
        val nsUrl = NSURL(string = playback)

	    val item = AVPlayerItem(uRL = nsUrl)
        player.replaceCurrentItemWithPlayerItem(item)

        if (startPositionMs > 0 && !hasSeekedToStart) {
            player.seekToTime(
                CMTimeMakeWithSeconds(
                    startPositionMs / 1000.0,
                    NSEC_PER_SEC.toInt()
                )
            )

            hasSeekedToStart = true
        }

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

        endToken = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = null
        ) {
            currentOnPlaybackEnded?.invoke()
        }
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
            }

            /*
             * Keep the screen's play/pause state in sync with the native
             * transport controls. Anything that is not explicitly paused
             * (playing or buffering/waiting) counts as "playing".
             */
            val nowPlaying = player.timeControlStatus != AVPlayerTimeControlStatusPaused
            if (nowPlaying != lastReportedPlaying) {
                lastReportedPlaying = nowPlaying
                currentOnPlayPauseChange?.invoke(nowPlaying)
            }
        }

        onDispose {
            player.removeTimeObserver(timeObserver)
            player.pause()
            player.replaceCurrentItemWithPlayerItem(null)
            controller.player = null
            endToken?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            endToken = null
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
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

        /*
         * AVPlayerViewController does not expose native previous/next
         * transport controls, so we render our own overlay buttons on iOS
         * (Android uses the native playlist prev/next instead).
         */
        if (useController && onPrevious != null) {
            VideoOverlayButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = "Previous Exercise",
                onClick = { onPrevious?.invoke() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp),
                enabled = canGoPrevious
            )
        }

        if (useController && onNext != null) {
            VideoOverlayButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = "Next Exercise",
                onClick = { onNext?.invoke() },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                enabled = canGoNext
            )
        }
    }
}

