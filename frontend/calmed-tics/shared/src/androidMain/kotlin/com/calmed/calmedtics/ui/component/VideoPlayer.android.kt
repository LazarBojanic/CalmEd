package com.calmed.calmedtics.ui.component

import android.content.Context
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R as Media3R
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.video.download.DownloadUtil
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    hlsUrl: String,
    title: String?,
    modifier: Modifier,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    useController: Boolean,
    playlist: List<VideoPlaylistItem>?,
    onPositionChanged: ((Long) -> Unit)?,
    onDurationChanged: ((Long) -> Unit)?,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)?,
    onFullscreenToggle: ((Boolean) -> Unit)?,
    onPlaybackEnded: (() -> Unit)?,
    onPlayPauseChange: ((Boolean) -> Unit)?,
    onPlaylistIndexChanged: ((Int) -> Unit)?,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    autoPlayNext: Boolean,
    repeatCurrentExercise: Boolean,
    startPositionMs: Long,
    restartTrigger: Int
) {
    val context = LocalContext.current

    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
    val currentOnDurationChanged by rememberUpdatedState(onDurationChanged)
    val currentOnVideoOrientationChanged by rememberUpdatedState(onVideoOrientationChanged)
    val currentOnFullscreenToggle by rememberUpdatedState(onFullscreenToggle)
    val currentOnPlaybackEnded by rememberUpdatedState(onPlaybackEnded)
    val currentOnPlayPauseChange by rememberUpdatedState(onPlayPauseChange)
    val currentOnPlaylistIndexChanged by rememberUpdatedState(onPlaylistIndexChanged)

    val cacheDataSourceFactory = remember {
        DownloadUtil.getPlaybackDataSourceFactory(context)
    }

    val player = remember(cacheDataSourceFactory) {
        buildPlayer(context, cacheDataSourceFactory)
    }

    val states by LocalVideoDownloadManager.states.collectAsState()

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
                val isRotated =
                    videoSize.unappliedRotationDegrees == 90 ||
                        videoSize.unappliedRotationDegrees == 270

                val effectiveWidth = if (isRotated) videoSize.height else videoSize.width
                val effectiveHeight = if (isRotated) videoSize.width else videoSize.height

                if (effectiveWidth > 0 && effectiveHeight > 0) {
                    currentOnVideoOrientationChanged?.invoke(effectiveHeight >= effectiveWidth)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                currentOnPlayPauseChange?.invoke(isPlaying)

                if (!isPlaying) {
                    currentOnPositionChanged?.invoke(player.currentPosition)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentOnPlaylistIndexChanged?.invoke(player.currentMediaItemIndex)
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    val playlistItems = remember(playlist) {
        playlist ?: listOf(VideoPlaylistItem(url = hlsUrl, title = title))
    }

    var hasAppliedStartPosition by remember { mutableStateOf(false) }

    LaunchedEffect(playlistItems, hlsUrl) {
        playlistItems.forEach { item ->
            LocalVideoDownloadManager.refresh(item.url)
        }

        val mediaItems = playlistItems.map { item ->
            if (states.stateFor(item.url).status == VideoDownloadStatus.Downloaded) {
                LocalVideoDownloadManager.downloadedMediaItem(item.url)
                    ?: MediaItem.fromUri(item.url.toUri())
            } else {
                MediaItem.fromUri(item.url.toUri())
            }
        }

        val startIndex = playlistItems.indexOfFirst { it.url == hlsUrl }.coerceAtLeast(0)

        val needsReload =
            player.mediaItemCount != mediaItems.size ||
                player.currentMediaItem?.localConfiguration?.uri !=
                mediaItems[startIndex].localConfiguration?.uri

        if (needsReload) {
            player.setMediaItems(mediaItems, startIndex, 0)

            if (startPositionMs > 0 && !hasAppliedStartPosition) {
                player.seekTo(startIndex, startPositionMs)
                hasAppliedStartPosition = true
            }

            player.prepare()
        }
    }

    LaunchedEffect(isPlaying, isMuted, restartTrigger, autoPlayNext, repeatCurrentExercise) {
        player.pauseAtEndOfMediaItems = !autoPlayNext
        player.repeatMode =
            if (repeatCurrentExercise) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF

        player.playWhenReady = isPlaying
        if (isPlaying) player.play() else player.pause()

        player.volume = if (isMuted) 0f else 1f

        if (restartTrigger > 0) {
            player.seekTo(0L)
            player.play()
        }
    }

    LaunchedEffect(player) {
        while (true) {
            if (player.isPlaying) {
                currentOnPositionChanged?.invoke(player.currentPosition)
            }
            delay(500.milliseconds)
        }
    }

    PlayerContent(
        modifier = modifier,
        player = player,
        useController = useController,
        onFullscreenToggle = currentOnFullscreenToggle
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
    Box(
        modifier = modifier.background(Color.Black)
    ) {
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
    context: Context,
    cacheFactory: CacheDataSource.Factory
): ExoPlayer {
    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
        .build()
}

@OptIn(UnstableApi::class)
private fun createPlayerView(
    ctx: Context,
    player: ExoPlayer,
    useController: Boolean,
    onFullscreenToggle: ((Boolean) -> Unit)?
): PlayerView {
    return PlayerView(ctx).apply {
        this.player = player
        this.useController = useController

        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)

        setShowFastForwardButton(true)
        setShowRewindButton(true)
        setShowNextButton(true)
        setShowPreviousButton(true)
        setShowSubtitleButton(false)

        controllerAutoShow = true
        controllerShowTimeoutMs = 3000
        controllerHideOnTouch = true

        findViewById<View>(Media3R.id.exo_settings)?.visibility = View.GONE

        if (onFullscreenToggle != null) {
            setFullscreenButtonClickListener { isFullScreen ->
                onFullscreenToggle(isFullScreen)
            }
        }

        post {
            if (useController) {
                showController()
            }
        }
    }
}

