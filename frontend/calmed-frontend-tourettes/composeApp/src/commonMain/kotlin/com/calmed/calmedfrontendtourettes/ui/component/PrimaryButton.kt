package com.calmed.calmedfrontendtourettes.ui.component
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun PrimaryButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	Button(
		onClick = onClick,
		enabled = enabled,
		modifier = Modifier
			.fillMaxWidth()
			.defaultMinSize(minHeight = 48.dp)
			.padding(horizontal = 16.dp)
			.then(modifier),
		colors = ButtonDefaults.buttonColors(
			containerColor = MaterialTheme.colorScheme.primary,
			contentColor = MaterialTheme.colorScheme.onPrimary,
			disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
			disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
		)
	) {
		Text(text)
	}
}