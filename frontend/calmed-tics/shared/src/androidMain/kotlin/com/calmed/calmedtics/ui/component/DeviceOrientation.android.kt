package com.calmed.calmedtics.ui.component

import android.provider.Settings
import android.view.OrientationEventListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDeviceLandscape(): Boolean {
	val context = LocalContext.current
	var landscape by remember { mutableStateOf(false) }

	DisposableEffect(context) {
		val rotationEnabled = runCatching {
			Settings.System.getInt(
				context.contentResolver,
				Settings.System.ACCELEROMETER_ROTATION,
				0,
			) == 1
		}.getOrDefault(true)

		if (!rotationEnabled) {
			landscape = false
			return@DisposableEffect onDispose { }
		}

		val listener = object : OrientationEventListener(context) {
			override fun onOrientationChanged(orientation: Int) {
				if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return
				val isLandscape = orientation in 45..135 || orientation in 225..315
				if (isLandscape != landscape) {
					landscape = isLandscape
				}
			}
		}

		if (listener.canDetectOrientation()) {
			listener.enable()
		}

		onDispose { listener.disable() }
	}

	return landscape
}
