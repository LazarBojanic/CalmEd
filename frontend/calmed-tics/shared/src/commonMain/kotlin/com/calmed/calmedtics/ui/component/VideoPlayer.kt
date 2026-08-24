package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    hlsUrl: String,
    title: String? = null,
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
    onPlayPauseChange: ((Boolean) -> Unit)? = null,
    restartTrigger: Int = 0
)

