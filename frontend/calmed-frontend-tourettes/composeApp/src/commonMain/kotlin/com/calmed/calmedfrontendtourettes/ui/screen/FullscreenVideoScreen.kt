package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.calmed.calmedfrontendtourettes.ui.component.VideoPlayer

@Composable
fun FullscreenVideoScreen(
    hlsUrl: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            VideoPlayer(
                hlsUrl = hlsUrl,
                modifier = Modifier.fillMaxSize(),
                isFullscreen = true,
                onFullscreenToggle = onBack
            )
        }
    }
}
