package com.calmed.calmedfrontendtourettes.ui.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.calmed.calmedfrontendtourettes.service.specification.LocalVideoDownloadManager
import com.calmed.calmedfrontendtourettes.service.specification.VideoDownloadStatus
import com.calmed.calmedfrontendtourettes.service.specification.stateFor
import com.calmed.calmedfrontendtourettes.video.download.DownloadUtil
import kotlinx.coroutines.flow.collectLatest

@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier,
    isFullscreen: Boolean,
    onFullscreenToggle: (() -> Unit)?
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val cacheDataSourceFactory = remember(context) {
        DownloadUtil.getPlaybackDataSourceFactory(context)
    }
    val player = remember(context, cacheDataSourceFactory) {
        buildPlayer(context, cacheDataSourceFactory)
    }

    val states by LocalVideoDownloadManager.states.collectAsStateCompat()
    val downloadState = states.stateFor(hlsUrl)
    val onDownloadClick: () -> Unit = {
        if (downloadState.status == VideoDownloadStatus.NotDownloaded || downloadState.status == VideoDownloadStatus.Failed) {
            LocalVideoDownloadManager.download(hlsUrl)
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(hlsUrl, downloadState.status) {
        LocalVideoDownloadManager.refresh(hlsUrl)

        val playbackUrl = LocalVideoDownloadManager.playbackUrl(hlsUrl)
        val targetUri = Uri.parse(playbackUrl)
        val currentUri = player.currentMediaItem?.localConfiguration?.uri

        if (currentUri != targetUri) {
            val resumePositionMs = player.currentPosition
            player.setMediaItem(MediaItem.fromUri(targetUri))
            player.prepare()
            if (resumePositionMs > 0L) {
                player.seekTo(resumePositionMs)
            }
        }

        player.playWhenReady = true
    }

    PlayerContent(
        modifier = modifier,
        player = player,
        downloadStatus = downloadState.status,
        isFullscreen = isFullscreen,
        onFullscreenToggle = onFullscreenToggle,
        onDownloadClick = onDownloadClick
    )
}

@Composable
private fun PlayerContent(
    modifier: Modifier,
    player: ExoPlayer,
    downloadStatus: VideoDownloadStatus,
    isFullscreen: Boolean,
    onFullscreenToggle: (() -> Unit)?,
    onDownloadClick: () -> Unit
) {
    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> createPlayerView(ctx, player) },
            update = { playerView ->
                playerView.player = player
                playerView.showController()
            }
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DownloadButton(
                status = downloadStatus,
                onClick = onDownloadClick
            )

            if (onFullscreenToggle != null) {
                Spacer(modifier = Modifier.size(8.dp))
                FullscreenToggleButton(
                    isFullscreen = isFullscreen,
                    onClick = onFullscreenToggle
                )
            }
        }
    }
}

private fun buildPlayer(context: android.content.Context, cacheFactory: CacheDataSource.Factory): ExoPlayer {
    val mediaSourceFactory = DefaultMediaSourceFactory(cacheFactory)
    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .build()
}

private fun createPlayerView(
    ctx: android.content.Context,
    player: ExoPlayer
): PlayerView {
    return PlayerView(ctx).apply {
        this.player = player
        useController = true
        setControllerShowTimeoutMs(3_000)
        setControllerHideOnTouch(false)
        setControllerAutoShow(true)
        setControllerAnimationEnabled(false)
        setShowSubtitleButton(true)
        setShowNextButton(false)
        setShowPreviousButton(false)
        setShowShuffleButton(false)
        setShowVrButton(false)
        showController()
    }
}

@Composable
private fun DownloadButton(
    status: VideoDownloadStatus,
    onClick: () -> Unit
) {
    val isEnabled = status == VideoDownloadStatus.NotDownloaded || status == VideoDownloadStatus.Failed

    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        when (status) {
            VideoDownloadStatus.NotDownloaded -> {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = Color.White
                )
            }

            VideoDownloadStatus.Downloading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }

            VideoDownloadStatus.Downloaded -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Downloaded",
                    tint = Color(0xFF4CAF50)
                )
            }

            VideoDownloadStatus.Failed -> {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Download failed",
                    tint = Color(0xFFFF8A80)
                )
            }
        }
    }
}

@Composable
private fun FullscreenToggleButton(
    isFullscreen: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = true,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        Icon(
            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
            tint = Color.White
        )
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateCompat(): androidx.compose.runtime.State<T> {
    val flow = this
    val state = remember(flow) { mutableStateOf(flow.value) }
    LaunchedEffect(flow) {
        flow.collectLatest { state.value = it }
    }
    return state
}
