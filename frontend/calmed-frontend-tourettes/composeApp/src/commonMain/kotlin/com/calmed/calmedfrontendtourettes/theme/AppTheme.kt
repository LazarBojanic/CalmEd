package com.calmed.calmedfrontendtourettes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable() () -> Unit
) {
	val colorScheme = if (darkTheme) darkScheme else lightScheme
	MaterialTheme(
		colorScheme = colorScheme,
		typography = AppTypography,
		content = content
	)
}