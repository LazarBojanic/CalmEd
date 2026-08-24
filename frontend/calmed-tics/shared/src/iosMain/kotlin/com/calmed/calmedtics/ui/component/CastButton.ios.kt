package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVKit.AVRoutePickerView
import platform.UIKit.UIColor

/**
 * Native AirPlay route picker button. AVPlayerViewController already exposes
 * AirPlay through its own transport bar, but this makes casting available
 * from our overlay even when the native controls are hidden, and it keeps the
 * shared CastButton slot functional on iOS.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CastButton(
    hlsUrl: String,
    title: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                AVRoutePickerView().apply {
                    backgroundColor = UIColor.clearColor
                    tintColor = UIColor.whiteColor
                }
            },
            properties = UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true
            )
        )
    }
}
