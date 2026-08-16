package com.calmed.calmedtics.ui.component

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.video.download.DownloadUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    modifier: Modifier,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    useController: Boolean,
    onPositionChanged: ((Long) -> Unit)?,
    onDurationChanged: ((Long) -> Unit)?,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)?,
    onFullscreenToggle: ((Boolean) -> Unit)?,
    onPlaybackEnded: (() -> Unit)?,
    restartTrigger: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
    val currentOnDurationChanged by rememberUpdatedState(onDurationChanged)
    val currentOnVideoOrientationChanged by rememberUpdatedState(onVideoOrientationChanged)
    val currentOnFullscreenToggle by rememberUpdatedState(onFullscreenToggle)
    val currentOnPlaybackEnded by rememberUpdatedState(onPlaybackEnded)

    val cacheDataSourceFactory = remember {
        DownloadUtil.getPlaybackDataSourceFactory(context)
    }
    val player = remember(cacheDataSourceFactory) {
        buildPlayer(context, cacheDataSourceFactory)
    }

    val states by LocalVideoDownloadManager.states.collectAsStateCompat()
    val downloadState = states.stateFor(hlsUrl)

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val duration = player.duration
                if (duration > 0) {
                    currentOnDurationChanged?.invoke(duration)
                }
                if (playbackState == Player.STATE_ENDED) {
                    currentOnPlaybackEnded?.invoke()
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                val isRotated = videoSize.unappliedRotationDegrees == 90 || videoSize.unappliedRotationDegrees == 270
                val effectiveWidth = if (isRotated) videoSize.height else videoSize.width
                val effectiveHeight = if (isRotated) videoSize.width else videoSize.height
                if (effectiveWidth > 0 && effectiveHeight > 0) {
                    val isPortrait = effectiveHeight >= effectiveWidth
                    currentOnVideoOrientationChanged?.invoke(isPortrait)
                }
            }
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(hlsUrl, downloadState.status) {
        LocalVideoDownloadManager.refresh(hlsUrl)

        val playbackUrl = LocalVideoDownloadManager.playbackUrl(hlsUrl)
        val targetUri = Uri.parse(playbackUrl)
        val currentUri = player.currentMediaItem?.localConfiguration?.uri

        if (currentUri != targetUri) {
            player.setMediaItem(MediaItem.fromUri(targetUri))
            player.prepare()
        }
    }

    LaunchedEffect(isPlaying, player) {
        player.playWhenReady = isPlaying
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }

    LaunchedEffect(isMuted, player) {
        player.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(restartTrigger) {
        if (restartTrigger > 0) {
            player.seekTo(0L)
            player.play()
        }
    }

    LaunchedEffect(player) {
        while (true) {
            val duration = player.duration
            val position = player.currentPosition

            if (duration > 0) {
                currentOnDurationChanged?.invoke(duration)
            }
            currentOnPositionChanged?.invoke(position)

            delay(500)
        }
    }

    PlayerContent(
        modifier = modifier,
        player = player,
        useController = useController,
        onFullscreenToggle = currentOnFullscreenToggle
    )
}

@Composable
actual fun VideoPlayerWithState(
    hlsUrl: String,
    modifier: Modifier,
    isPlaying: Boolean,
    isMuted: Boolean,
    useController: Boolean,
    onPositionChanged: (Long) -> Unit,
    onDurationChanged: (Long) -> Unit,
    restartTrigger: Int,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)?,
    onFullscreenToggle: ((Boolean) -> Unit)?,
    onPlaybackEnded: (() -> Unit)?,
    isFullscreen: Boolean
) {
    VideoPlayer(
        hlsUrl = hlsUrl,
        modifier = modifier,
        isFullscreen = isFullscreen,
        isPlaying = isPlaying,
        isMuted = isMuted,
        useController = useController,
        onPositionChanged = onPositionChanged,
        onDurationChanged = onDurationChanged,
        onVideoOrientationChanged = onVideoOrientationChanged,
        onFullscreenToggle = onFullscreenToggle,
        onPlaybackEnded = onPlaybackEnded,
        restartTrigger = restartTrigger
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerContent(
    modifier: Modifier,
    player: ExoPlayer,
    useController: Boolean,
    onFullscreenToggle: ((Boolean) -> Unit)?
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                createPlayerView(ctx, player, useController, onFullscreenToggle)
            },
            update = { playerView ->
                playerView.player = player
                playerView.useController = useController
                if (onFullscreenToggle != null) {
                    playerView.setFullscreenButtonClickListener { isFullScreen ->
                        onFullscreenToggle(isFullScreen)
                    }
                } else {
                    playerView.setFullscreenButtonClickListener(null)
                }
            }
        )
    }
}

@OptIn(UnstableApi::class)
private fun buildPlayer(
    context: android.content.Context,
    cacheFactory: CacheDataSource.Factory
): ExoPlayer {
    val mediaSourceFactory = DefaultMediaSourceFactory(cacheFactory)
    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .build()
}

@OptIn(UnstableApi::class)
private fun createPlayerView(
    ctx: android.content.Context,
    player: ExoPlayer,
    useController: Boolean,
    onFullscreenToggle: ((Boolean) -> Unit)?
): PlayerView {
    return PlayerView(ctx).apply {
        this.player = player
        this.useController = useController
        this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
        setShowFastForwardButton(true)
        setShowRewindButton(true)
        setShowNextButton(false)
        setShowPreviousButton(false)
        setShowSubtitleButton(true)
        controllerShowTimeoutMs = 3000
        controllerHideOnTouch = true
        if (onFullscreenToggle != null) {
            setFullscreenButtonClickListener { isFullScreen ->
                onFullscreenToggle(isFullScreen)
            }
        }
    }
}

@Composable
private fun <T> StateFlow<T>.collectAsStateCompat(): State<T> {
    val flow = this
    val state = remember(flow) { mutableStateOf(flow.value) }
    LaunchedEffect(flow) {
        flow.collectLatest { state.value = it }
    }
    return state
}
