package com.calmed.calmedtics.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush

@Composable
fun appBackgroundGradient(): Brush {
	val scheme = MaterialTheme.colorScheme
	return remember(scheme) {
		Brush.verticalGradient(
			colors = listOf(
				scheme.secondaryContainer,
				scheme.tertiaryContainer,
			)
		)
	}
}
