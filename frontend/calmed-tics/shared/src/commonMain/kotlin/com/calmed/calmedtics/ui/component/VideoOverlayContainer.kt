package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun VideoOverlayContainer(
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
	content: @Composable BoxScope.() -> Unit,
) {
	Box(
		modifier = modifier
			.size(48.dp)
			.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
		contentAlignment = Alignment.Center,
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(CircleShape)
				.background(
					MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)
				),
		)
		content()
	}
}
