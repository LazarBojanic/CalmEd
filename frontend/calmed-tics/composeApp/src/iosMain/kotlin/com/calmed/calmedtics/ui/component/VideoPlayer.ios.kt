package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.NSLayoutAttributeBottom
import platform.UIKit.NSLayoutAttributeLeading
import platform.UIKit.NSLayoutAttributeTop
import platform.UIKit.NSLayoutAttributeTrailing
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSLayoutRelationEqual
import platform.UIKit.UIView
import platform.darwin.NSObjectProtocol

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    onFullscreenToggle: (() -> Unit)?
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
    val tokens = remember { mutableStateListOf<NSObjectProtocol>() }

    val states by LocalVideoDownloadManager.states.collectAsState()
    val downloadState = states.stateFor(hlsUrl)

    LaunchedEffect(Unit) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
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
        if (isPlaying) {
            player.play()
        }

        val endToken = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = null
        ) {
            player.pause()
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

    DisposableEffect(Unit) {
        onDispose {
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

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DownloadButton(
                status = downloadState.status,
                onClick = {
                    if (downloadState.status == VideoDownloadStatus.NotDownloaded || downloadState.status == VideoDownloadStatus.Failed) {
                        LocalVideoDownloadManager.download(hlsUrl)
                    }
                }
            )

            if (onFullscreenToggle != null) {
                Spacer(modifier = Modifier.size(8.dp))
                FullscreenToggleButton(
                    isFullscreen = isFullscreen,
                    onClick = onFullscreenToggle
                )
            }
        }
    }
}

@Composable
actual fun VideoPlayerWithState(
    hlsUrl: String,
    modifier: Modifier,
    isPlaying: Boolean,
    isMuted: Boolean,
    onPositionChanged: (Long) -> Unit,
    onDurationChanged: (Long) -> Unit,
    restartTrigger: Int
) {
    // TODO: Implementation for iOS
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface))
}

@Composable
private fun DownloadButton(
    status: VideoDownloadStatus,
    onClick: () -> Unit
) {
    val isEnabled = status == VideoDownloadStatus.NotDownloaded || status == VideoDownloadStatus.Failed

    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
    ) {
        when (status) {
            VideoDownloadStatus.NotDownloaded -> Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.onSurface
            )

            VideoDownloadStatus.Downloading -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurface
            )

            VideoDownloadStatus.Downloaded -> Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Downloaded",
                tint = MaterialTheme.colorScheme.tertiary
            )

            VideoDownloadStatus.Failed -> Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Download failed",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun FullscreenToggleButton(
    isFullscreen: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
    ) {
        Icon(
            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
            tint = MaterialTheme.colorScheme.surface
        )
    }
}
