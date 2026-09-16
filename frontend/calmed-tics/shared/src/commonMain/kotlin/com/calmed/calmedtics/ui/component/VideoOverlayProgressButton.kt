package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun VideoOverlayProgressButton(
	progress: Float,
	contentDescription: String? = null,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	VideoOverlayContainer(
		modifier = modifier,
		onClick = if (enabled) onClick else null,
	) {
		CircularProgressIndicator(
			progress = { progress.coerceIn(0f, 1f) },
			modifier = Modifier
				.size(22.dp)
				.alpha(if (enabled) 1f else 0.4f),
			color = MaterialTheme.colorScheme.primary,
			trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
			strokeWidth = 2.dp,
		)
	}
}
