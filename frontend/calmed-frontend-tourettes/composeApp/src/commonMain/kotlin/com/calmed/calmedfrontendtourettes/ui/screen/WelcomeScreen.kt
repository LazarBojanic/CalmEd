package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.ui.component.MessageCard
import com.calmed.calmedfrontendtourettes.viewmodel.WelcomeViewModel
import org.koin.compose.koinInject

@Composable
fun WelcomeScreen(
	viewModel: WelcomeViewModel = koinInject()
) {
	val messages by viewModel.messages.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val error by viewModel.error.collectAsState()

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		when {
			isLoading -> {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center)
				)
			}

			error != null -> {
				Text(
					text = error!!,
					color = MaterialTheme.colorScheme.error,
					modifier = Modifier.align(Alignment.Center)
				)
			}

			else -> {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(vertical = 8.dp)
				) {
					items(
						items = messages,
						key = { it.id }
					) { message ->
						MessageCard(message)
					}
				}
			}
		}
	}
}