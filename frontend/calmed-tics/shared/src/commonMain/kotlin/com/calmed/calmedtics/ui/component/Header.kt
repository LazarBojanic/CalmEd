package com.calmed.calmedtics.ui.component
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Header(
	title: String,
	onBack: (() -> Unit)? = null,
	actions: @Composable RowScope.() -> Unit = {}
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding(),
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
				verticalAlignment = Alignment.CenterVertically
			) {
				if (onBack != null) {
					BackButton(onClick = onBack)
				}
				Text(
					text = title,
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.padding(start = if (onBack != null) 4.dp else 0.dp).weight(1f)
				)

				actions()
			}
		}
	}
}