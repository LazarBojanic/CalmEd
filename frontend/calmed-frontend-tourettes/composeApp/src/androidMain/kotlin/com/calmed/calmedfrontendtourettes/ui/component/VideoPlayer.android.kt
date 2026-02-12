package com.calmed.calmedfrontendtourettes.ui.component

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    val player = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LaunchedEffect(hlsUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(hlsUrl))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx -> PlayerView(ctx).apply { this.player = player } },
        update = { view -> view.player = player }
    )
}
