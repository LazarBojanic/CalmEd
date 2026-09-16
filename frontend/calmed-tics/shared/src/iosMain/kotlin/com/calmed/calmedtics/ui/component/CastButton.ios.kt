package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVKit.AVRoutePickerView
import platform.UIKit.UIColor

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CastButton(modifier: Modifier) {
	VideoOverlayContainer(modifier = modifier) {
		UIKitView(
			modifier = Modifier.size(40.dp),
			factory = {
				AVRoutePickerView().apply {
					backgroundColor = UIColor.clearColor
				}
			},
			properties = UIKitInteropProperties(
				isInteractive = true,
				isNativeAccessibilityEnabled = true
			)
		)
	}
}
