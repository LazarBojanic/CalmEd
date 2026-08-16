package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    isPlaying: Boolean = true,
    isMuted: Boolean = false,
    useController: Boolean = true,
    onPositionChanged: ((Long) -> Unit)? = null,
    onDurationChanged: ((Long) -> Unit)? = null,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)? = null,
    onFullscreenToggle: ((Boolean) -> Unit)? = null,
    onPlaybackEnded: (() -> Unit)? = null,
    restartTrigger: Int = 0
)

@Composable
expect fun VideoPlayerWithState(
    hlsUrl: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    isMuted: Boolean = false,
    useController: Boolean = true,
    onPositionChanged: (Long) -> Unit = {},
    onDurationChanged: (Long) -> Unit = {},
    restartTrigger: Int = 0,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)? = null,
    onFullscreenToggle: ((Boolean) -> Unit)? = null,
    onPlaybackEnded: (() -> Unit)? = null,
    isFullscreen: Boolean = false
)
