package com.calmed.calmedfrontendtourettes.ui.component

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current

    var isFullscreen by remember { mutableStateOf(false) }

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(hlsUrl) {
        player.setMediaItem(MediaItem.fromUri(Uri.parse(hlsUrl)))
        player.prepare()
        player.playWhenReady = true
    }

    // Normalni (ne-fullscreen) player
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            },
            update = { pv -> pv.player = player }
        )

        IconButton(
            onClick = { isFullscreen = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .zIndex(2f)
        ) {
            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen")
        }
    }

    if (isFullscreen) {

        // Back dugme zatvara fullscreen
        BackHandler(enabled = true) {
            isFullscreen = false
        }

        // sakrij system bars dok je fullscreen, vrati kad izadjes
        DisposableEffect(isFullscreen) {
            val window = activity?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, view)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            onDispose {
                if (window != null) {
                    val controller = WindowInsetsControllerCompat(window, view)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                }
            }
        }

        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                        }
                    },
                    update = { pv -> pv.player = player }
                )

                // ✅ X dugme gore desno (uvek iznad svega)
                IconButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .zIndex(10f)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}