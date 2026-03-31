package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.ui.component.VideoPlayer
import org.koin.compose.koinInject

@Composable
fun WelcomeVideoScreen(
    onSkip: () -> Unit,
    onContinue: (Boolean) -> Unit,
    onOpenFullscreen: (String) -> Unit
) {
    val appApi: IAppApi = koinInject()

    var dontShowAgain by remember { mutableStateOf(false) }

    var videoUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        error = null
        videoUrl = null

        try {
            val welcomeVideo = appApi.getWelcomeVideo()
            if (welcomeVideo == null) {
                error = "Welcome video is not available."
                return@LaunchedEffect
            }
            videoUrl = welcomeVideo.videoURL
            if (videoUrl == null) {
                error = "Welcome video has no valid playback source."
            }
        } catch (t: Throwable) {
            error = t.message ?: "Failed to load welcome video."
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val url = videoUrl
            if (url != null) {
                VideoPlayer(
                    hlsUrl = url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    onFullscreenToggle = { onOpenFullscreen(url) }
                )
            } else {
                Text(error ?: "Loading video...")
            }

            Text(
                "Welcome Video Screen",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleLarge
            )

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

            Button(
                onClick = onSkip,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Skip")
            }

            Button(
                onClick = { onContinue(dontShowAgain) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Continue")
            }
        }
    }
}