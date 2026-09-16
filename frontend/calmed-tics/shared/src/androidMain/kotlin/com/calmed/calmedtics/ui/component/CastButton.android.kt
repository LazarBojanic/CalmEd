package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.cast.MediaRouteButton
import androidx.media3.cast.rememberMediaRouteButtonState

@Composable
actual fun CastButton(modifier: Modifier) {
	VideoOverlayContainer(modifier = modifier) {
		CompositionLocalProvider(
			LocalContentColor provides MaterialTheme.colorScheme.onSurface,
			LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
		) {
			MediaRouteButton(
				modifier = Modifier.size(48.dp),
				state = rememberMediaRouteButtonState(),
			)
		}
	}
}
