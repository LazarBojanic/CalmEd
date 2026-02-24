package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import com.calmed.calmedfrontendtourettes.ui.component.VideoPlayer

@Composable
fun WelcomeVideoScreen(
    onSkip: () -> Unit,
    onContinue: (Boolean) -> Unit,
    onOpenFullscreen: (String) -> Unit
) {
    val videoUrl = "https://bombona.rs/videos/testvideo/testvideo.m3u8"
    var dontShowAgain by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        VideoPlayer(
            hlsUrl = videoUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            onFullscreenToggle = { onOpenFullscreen(videoUrl) }
        )
        Text("Welcome Video Screen")
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Checkbox(
                checked = dontShowAgain,
                onCheckedChange = { dontShowAgain = it }
            )
            Text(
                "Don't show again",
                modifier = Modifier.padding(start = 8.dp, top = 12.dp)
            )
        }
        Button(onClick = onSkip, modifier = Modifier.padding(top = 16.dp)) {
            Text("Skip")
        }

        Button(onClick = { onContinue(dontShowAgain) }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Continue")
        }
    }
}
