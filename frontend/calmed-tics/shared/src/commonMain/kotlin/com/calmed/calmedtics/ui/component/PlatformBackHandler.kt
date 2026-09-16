package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformBackHandler(
	enabled: Boolean = true,
	onBack: () -> Unit,
)
