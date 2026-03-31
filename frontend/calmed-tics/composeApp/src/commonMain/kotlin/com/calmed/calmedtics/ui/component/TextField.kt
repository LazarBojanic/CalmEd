package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TextField(
	value: String,
	onValueChange: (String) -> Unit,
	label: String,
	modifier: Modifier = Modifier,
	singleLine: Boolean = true,
	isError: Boolean = false,
	supportingText: String? = null,
) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		label = { Text(label) },
		singleLine = singleLine,
		isError = isError,
		supportingText = supportingText?.let { { Text(it) } },
		modifier = Modifier
			.fillMaxWidth()
			.then(modifier)
	)
}