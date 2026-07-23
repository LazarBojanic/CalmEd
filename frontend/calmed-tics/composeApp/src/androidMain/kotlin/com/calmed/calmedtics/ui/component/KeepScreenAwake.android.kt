package com.calmed.calmedtics.ui.component

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.activity.compose.LocalActivity


@Composable
actual fun KeepScreenAwake(enabled: Boolean) {
    val activity = LocalActivity.current

    DisposableEffect(enabled, activity) {
        if (enabled) {
            activity?.window?.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        } else {
            activity?.window?.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        onDispose {
            activity?.window?.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}