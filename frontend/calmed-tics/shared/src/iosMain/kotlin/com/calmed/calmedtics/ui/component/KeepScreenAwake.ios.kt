package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenAwake(enabled: Boolean) {
    DisposableEffect(enabled) {
        if (enabled) {
            UIApplication.sharedApplication.idleTimerDisabled = true
        }
        onDispose {
            UIApplication.sharedApplication.idleTimerDisabled = false
        }
    }
}
