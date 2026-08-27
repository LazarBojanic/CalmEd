package com.calmed.calmedtics.ui.component

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun FullscreenEffect(
    isFullscreen: Boolean,
    isVideoPortrait: Boolean,
    onDeviceOrientationChanged: ((isLandscape: Boolean) -> Unit)?
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnOrientationChanged by rememberUpdatedState(onDeviceOrientationChanged)

    DisposableEffect(context) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val isLandscape = orientation in 45..135 || orientation in 225..315
                currentOnOrientationChanged?.invoke(isLandscape)
            }
        }
        listener.enable()
        onDispose { listener.disable() }
    }

    DisposableEffect(isFullscreen, isVideoPortrait, activity) {
        applyOrientationAndSystemBars(activity, isFullscreen, isVideoPortrait)

        onDispose {
            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity.window?.let { win ->
                    val controller = WindowCompat.getInsetsController(win, win.decorView)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }
}

private fun applyOrientationAndSystemBars(
    activity: Activity?,
    isFullscreen: Boolean,
    isVideoPortrait: Boolean
) {
    val window = activity?.window ?: return

    activity.requestedOrientation = when {
        isFullscreen && isVideoPortrait -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        isFullscreen -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    val controller = WindowCompat.getInsetsController(window, window.decorView)
    if (isFullscreen) {
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    } else {
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
