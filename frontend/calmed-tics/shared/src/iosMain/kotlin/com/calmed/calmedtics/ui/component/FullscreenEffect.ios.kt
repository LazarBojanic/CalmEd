package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable

@Composable
actual fun FullscreenEffect(
    isFullscreen: Boolean,
    isVideoPortrait: Boolean,
    onDeviceOrientationChanged: ((isLandscape: Boolean) -> Unit)?
) {
    // iOS AVPlayerViewController natively manages fullscreen and orientation presentation
}

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // No-op on iOS
}
