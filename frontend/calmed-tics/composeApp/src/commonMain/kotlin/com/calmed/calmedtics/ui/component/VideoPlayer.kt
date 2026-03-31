package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    onFullscreenToggle: (() -> Unit)? = null
)
