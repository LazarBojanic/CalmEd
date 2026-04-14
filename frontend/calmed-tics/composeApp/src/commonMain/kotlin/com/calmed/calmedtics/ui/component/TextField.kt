package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
		label = {
			Text(
				text = label,
				color = Color.White.copy(alpha = 0.75f)
			)
		},
		singleLine = singleLine,
		isError = isError,
		supportingText = supportingText?.let {
			{
				Text(
					text = it,
					color = Color.White.copy(alpha = 0.7f)
				)
			}
		},
		textStyle = MaterialTheme.typography.bodyLarge.copy(
			color = Color.White
		),
		shape = MaterialTheme.shapes.extraLarge,
		colors = OutlinedTextFieldDefaults.colors(
			focusedTextColor = Color.White,
			unfocusedTextColor = Color.White,
			focusedLabelColor = Color.White,
			unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
			cursorColor = Color.White,

			focusedBorderColor = Color.White.copy(alpha = 0.65f),
			unfocusedBorderColor = Color.White.copy(alpha = 0.35f),

			focusedContainerColor = Color.White.copy(alpha = 0.10f),
			unfocusedContainerColor = Color.White.copy(alpha = 0.06f),

			errorTextColor = Color.White,
			errorLabelColor = Color.White,
			errorBorderColor = Color.White.copy(alpha = 0.8f),
			errorCursorColor = Color.White,
			errorSupportingTextColor = Color.White
		),
		modifier = Modifier
			.fillMaxWidth()
			.then(modifier)
	)
}