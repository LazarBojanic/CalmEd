package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenScaffold(
	title: String,
	onBack: (() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(Modifier.fillMaxSize()) {
		Header(title = title, onBack = onBack)
		Column(Modifier.fillMaxSize().padding(16.dp)) {
			content()
		}
	}
}