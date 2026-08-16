package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable

@Composable
expect fun FullscreenEffect(
    isFullscreen: Boolean,
    isVideoPortrait: Boolean,
    onDeviceOrientationChanged: ((isLandscape: Boolean) -> Unit)? = null
)

@Composable
expect fun PlatformBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)
