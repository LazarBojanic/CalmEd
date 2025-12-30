package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.model.joined.MessageJoined

@Composable
fun MessageCard(
	message: MessageJoined,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
	) {
		Text(
			text = message.text ?: "",
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.padding(16.dp)
		)
	}
}