package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.ic_google_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun GoogleSignInButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	text: String = "Continue with Google",
) {
	OutlinedButton(
		onClick = onClick,
		enabled = enabled,
		modifier = Modifier
			.fillMaxWidth()
			.defaultMinSize(minHeight = 48.dp)
			.padding(horizontal = 16.dp)
			.then(modifier),
		colors = ButtonDefaults.outlinedButtonColors(
			containerColor = MaterialTheme.colorScheme.surface,
			contentColor = MaterialTheme.colorScheme.onSurface,
			disabledContainerColor = MaterialTheme.colorScheme.surface,
			disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
		),
		border = ButtonDefaults.outlinedButtonBorder.copy(
			brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
		),
		shape = MaterialTheme.shapes.medium,
		contentPadding = ButtonDefaults.ContentPadding
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
		) {
			Image(
				painter = painterResource(Res.drawable.ic_google_logo),
				contentDescription = null,
				modifier = Modifier.size(18.dp)
			)
			Spacer(Modifier.width(12.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.labelLarge,
				fontWeight = FontWeight.Medium
			)
		}
	}
}