package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.calmed.calmedfrontendtourettes.ui.component.VideoPlayer

@Composable
fun FullscreenVideoScreen(
    hlsUrl: String,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        VideoPlayer(
            hlsUrl = hlsUrl,
            modifier = Modifier.fillMaxSize()
        )

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}