package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun TextField(
	value: String,
	onValueChange: (String) -> Unit,
	label: String,
	modifier: Modifier = Modifier,
	singleLine: Boolean = true,
	isError: Boolean = false,
	supportingText: String? = null,
	shape: Shape = OutlinedTextFieldDefaults.shape,
	colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
		focusedContainerColor = Color.White,
		unfocusedContainerColor = Color.White,
		errorContainerColor = Color.White,
	),
) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		label = { Text(label) },
		singleLine = singleLine,
		isError = isError,
		supportingText = supportingText?.let { { Text(it) } },
		shape = shape,
		colors = colors,
		modifier = Modifier
			.fillMaxWidth()
			.then(modifier)
	)
}