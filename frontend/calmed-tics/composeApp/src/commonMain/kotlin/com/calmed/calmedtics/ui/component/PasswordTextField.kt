package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordTextField(
	value: String,
	onValueChange: (String) -> Unit,
	label: String = "Password",
	modifier: Modifier = Modifier,
	singleLine: Boolean = true,
	isError: Boolean = false,
	supportingText: String? = null,
	shape: Shape = OutlinedTextFieldDefaults.shape,
	colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
		focusedContainerColor = MaterialTheme.colorScheme.surface,
		unfocusedContainerColor = MaterialTheme.colorScheme.surface,
		errorContainerColor = MaterialTheme.colorScheme.surface,
	),
) {
	val (showPassword, setShowPassword) = remember { mutableStateOf(false) }

	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		label = { Text(label) },
		singleLine = singleLine,
		isError = isError,
		supportingText = supportingText?.let { { Text(it) } },
		visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
		trailingIcon = {
			val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
			val description = if (showPassword) "Hide password" else "Show password"
			IconButton(onClick = { setShowPassword(!showPassword) }) {
				Icon(imageVector = image, contentDescription = description)
			}
		},
		shape = shape,
		colors = colors,
		modifier = Modifier
			.fillMaxWidth()
			.then(modifier)
	)
}