package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    isPlaying: Boolean,
    onFullscreenToggle: (() -> Unit)? = null
)

@Composable
expect fun VideoPlayerWithState(
    hlsUrl: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isMuted: Boolean,
    onPositionChanged: (Long) -> Unit,
    onDurationChanged: (Long) -> Unit,
    restartTrigger: Int = 0,

)
