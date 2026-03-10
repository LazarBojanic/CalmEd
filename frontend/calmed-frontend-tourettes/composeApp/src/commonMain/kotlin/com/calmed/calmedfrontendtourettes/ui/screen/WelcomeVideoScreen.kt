package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.http.AppHttpClient
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import com.calmed.calmedfrontendtourettes.ui.component.VideoPlayer
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data class PlaybackTokenResponse(
    val playbackId: String,
    val token: String
)

@Composable
fun WelcomeVideoScreen(
    onSkip: () -> Unit,
    onContinue: (Boolean) -> Unit,
    onOpenFullscreen: (String) -> Unit
) {

    val welcomePlaybackId = "YWsHmIT6VfzNtNdP16UTBOaNHYPuoWxgL00npms3Wopg"

    val appHttpClient: AppHttpClient = koinInject()
    val tokenStore: ITokenDataStore = koinInject()

    var dontShowAgain by remember { mutableStateOf(false) }

    var videoUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(welcomePlaybackId) {
        error = null
        videoUrl = null

        try {
            val access = tokenStore.getToken()?.access ?: run {
                error = "Missing access token."
                return@LaunchedEffect
            }

            val resp: PlaybackTokenResponse =
                appHttpClient.client
                    .get("/videos/$welcomePlaybackId/token") {
                        header("Authorization", "Bearer $access")
                    }
                    .body()

            videoUrl = "https://stream.mux.com/${resp.playbackId}.m3u8?token=${resp.token}"
        } catch (t: Throwable) {
            error = t.message ?: "Failed to load welcome video."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        Text("Welcome Video Screen", modifier = Modifier.padding(top = 12.dp))

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