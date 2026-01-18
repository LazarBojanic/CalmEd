package com.calmed.calmedfrontendtourettes.ui.component
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun Header(
	title: String,
	onBack: (() -> Unit)? = null
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.secondaryContainer,
		tonalElevation = 0.dp,
		shadowElevation = 0.dp
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp, vertical = 24.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, bottom = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Start
			) {
				if (onBack != null) {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
				Text(
					text = title,
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.padding(start = if (onBack != null) 4.dp else 0.dp)
				)
			}
		}
	}
}