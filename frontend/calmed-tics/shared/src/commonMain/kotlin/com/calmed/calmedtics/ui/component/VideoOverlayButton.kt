package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun VideoOverlayButton(
	icon: ImageVector,
	contentDescription: String? = null,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	VideoOverlayContainer(
		modifier = modifier,
		onClick = if (enabled) onClick else null,
	) {
		Icon(
			imageVector = icon,
			contentDescription = contentDescription,
			tint = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier
				.size(22.dp)
				.alpha(if (enabled) 1f else 0.4f),
		)
	}
}
