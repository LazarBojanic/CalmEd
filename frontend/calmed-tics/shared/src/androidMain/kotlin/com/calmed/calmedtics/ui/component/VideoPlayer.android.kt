package com.calmed.calmedtics.ui.component

import android.content.Context
import android.net.Uri
import android.widget.ImageButton
import android.widget.LinearLayout
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.media3.ui.R as Media3R
import com.calmed.calmedtics.shared.R
import com.calmed.calmedtics.service.specification.LocalVideoDownloadManager
import com.calmed.calmedtics.service.specification.VideoDownloadStatus
import com.calmed.calmedtics.service.specification.stateFor
import com.calmed.calmedtics.video.download.DownloadUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

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
    onPositionChanged: ((Long) -> Unit)?,
    onDurationChanged: ((Long) -> Unit)?,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)?,
    onFullscreenToggle: ((Boolean) -> Unit)?,
    onPlaybackEnded: (() -> Unit)?,
    onPlayPauseChange: ((Boolean) -> Unit)?,
    restartTrigger: Int
) {
    val context = LocalContext.current

    val currentOnPositionChanged by rememberUpdatedState(
        onPositionChanged
    )

    val currentOnDurationChanged by rememberUpdatedState(
        onDurationChanged
    )

    val currentOnVideoOrientationChanged by rememberUpdatedState(
        onVideoOrientationChanged
    )

    val currentOnFullscreenToggle by rememberUpdatedState(
        onFullscreenToggle
    )

    val currentOnPlaybackEnded by rememberUpdatedState(
        onPlaybackEnded
    )

    val currentOnPlayPauseChange by rememberUpdatedState(
        onPlayPauseChange
    )

    val cacheDataSourceFactory =
        remember {
            DownloadUtil.getPlaybackDataSourceFactory(context)
        }

    val player =
        remember(cacheDataSourceFactory) {
            buildPlayer(
                context = context,
                cacheFactory = cacheDataSourceFactory
            )
        }

    val states by
    LocalVideoDownloadManager.states
        .collectAsStateCompat()

    val downloadState =
        states.stateFor(hlsUrl)

    DisposableEffect(player) {
        val listener =
            object : Player.Listener {

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {
                    val duration = player.duration

                    if (duration > 0) {
                        currentOnDurationChanged
                            ?.invoke(duration)
                    }

                    if (
                        playbackState ==
                        Player.STATE_ENDED
                    ) {
                        currentOnPlaybackEnded
                            ?.invoke()
                    }
                }

                override fun onVideoSizeChanged(
                    videoSize: VideoSize
                ) {
                    val isRotated =
                        videoSize.unappliedRotationDegrees == 90 ||
                            videoSize.unappliedRotationDegrees == 270

                    val effectiveWidth =
                        if (isRotated) {
                            videoSize.height
                        } else {
                            videoSize.width
                        }

                    val effectiveHeight =
                        if (isRotated) {
                            videoSize.width
                        } else {
                            videoSize.height
                        }

                    if (
                        effectiveWidth > 0 &&
                        effectiveHeight > 0
                    ) {
                        val isPortrait =
                            effectiveHeight >= effectiveWidth

                        currentOnVideoOrientationChanged
                            ?.invoke(isPortrait)
                    }
                }

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {
                    currentOnPlayPauseChange
                        ?.invoke(isPlaying)
                }
            }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(
        hlsUrl,
        downloadState.status
    ) {
        LocalVideoDownloadManager.refresh(hlsUrl)

        val mediaItem =
            if (
                downloadState.status ==
                VideoDownloadStatus.Downloaded
            ) {
                LocalVideoDownloadManager
                    .downloadedMediaItem(hlsUrl)
                    ?: MediaItem.fromUri(
                        Uri.parse(hlsUrl)
                    )
            } else {
                MediaItem.fromUri(
                    Uri.parse(hlsUrl)
                )
            }

        val currentMediaItem =
            player.currentMediaItem

        if (
            currentMediaItem?.mediaId !=
            mediaItem.mediaId ||
            currentMediaItem?.localConfiguration?.uri !=
            mediaItem.localConfiguration?.uri ||
            currentMediaItem?.localConfiguration?.streamKeys !=
            mediaItem.localConfiguration?.streamKeys
        ) {
            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }

    LaunchedEffect(
        isPlaying,
        player
    ) {
        player.playWhenReady = isPlaying

        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }

    LaunchedEffect(
        isMuted,
        player
    ) {
        player.volume =
            if (isMuted) {
                0f
            } else {
                1f
            }
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
                currentOnDurationChanged
                    ?.invoke(duration)
            }

            currentOnPositionChanged
                ?.invoke(position)

            delay(500)
        }
    }

    PlayerContent(
        modifier = modifier,
        player = player,
        useController = useController,
        hlsUrl = hlsUrl,
        title = title,
        downloadState = downloadState.status,
        onFullscreenToggle =
            currentOnFullscreenToggle
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerContent(
    modifier: Modifier,
    player: ExoPlayer,
    useController: Boolean,
    hlsUrl: String,
    title: String?,
    downloadState: VideoDownloadStatus,
    onFullscreenToggle:
    ((Boolean) -> Unit)?
) {
    Box(
        modifier =
            modifier.background(
                MaterialTheme.colorScheme.surface
            )
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),

            factory = { ctx ->
                createPlayerView(
                    ctx = ctx,
                    player = player,
                    useController = useController,
                    hlsUrl = hlsUrl,
                    title = title,
                    downloadState = downloadState,
                    onFullscreenToggle =
                        onFullscreenToggle
                )
            },

            update = { playerView ->

                playerView.player = player


                playerView.useController =
                    useController

                playerView.controllerAutoShow =
                    true

                playerView.controllerShowTimeoutMs =
                    3000

                playerView.controllerHideOnTouch =
                    true

                updateDownloadButton(
                    playerView = playerView,
                    hlsUrl = hlsUrl,
                    title = title,
                    downloadState = downloadState
                )


                if (onFullscreenToggle != null) {
                    playerView.setFullscreenButtonClickListener {
                            isFullScreen ->
                        onFullscreenToggle(
                            isFullScreen
                        )
                    }
                } else {
                    playerView.setFullscreenButtonClickListener(
                        null
                    )
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
    val mediaSourceFactory =
        DefaultMediaSourceFactory(cacheFactory)

    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(
            mediaSourceFactory
        )
        .build()
}

@OptIn(UnstableApi::class)
private fun createPlayerView(
    ctx: Context,
    player: ExoPlayer,
    useController: Boolean,
    hlsUrl: String,
    title: String?,
    downloadState: VideoDownloadStatus,
    onFullscreenToggle:
    ((Boolean) -> Unit)?
): PlayerView {

    return PlayerView(ctx).apply {

        this.player = player

        this.useController =
            useController

        resizeMode =
            AspectRatioFrameLayout.RESIZE_MODE_FIT

        setShowBuffering(
            PlayerView.SHOW_BUFFERING_ALWAYS
        )


        setShowFastForwardButton(true)
        setShowRewindButton(true)

        setShowNextButton(false)
        setShowPreviousButton(false)

        setShowSubtitleButton(true)


        controllerAutoShow = true

        controllerShowTimeoutMs = 3000

        controllerHideOnTouch = true


        if (onFullscreenToggle != null) {
            setFullscreenButtonClickListener {
                    isFullScreen ->
                onFullscreenToggle(
                    isFullScreen
                )
            }
        }


        addDownloadButton(
            playerView = this,
            hlsUrl = hlsUrl,
            title = title,
            downloadState = downloadState
        )


        post {
            if (useController) {
                showController()
            }
        }
    }
}

@OptIn(UnstableApi::class)
private fun addDownloadButton(
    playerView: PlayerView,
    hlsUrl: String,
    title: String?,
    downloadState: VideoDownloadStatus
) {
    val basicControls =
        playerView.findViewById<LinearLayout>(
            Media3R.id.exo_basic_controls
        ) ?: return


    if (
        basicControls.findViewWithTag<ImageButton>(
            DOWNLOAD_BUTTON_TAG
        ) != null
    ) {
        return
    }

    val downloadButton =
        ImageButton(
            playerView.context
        ).apply {

            tag =
                DOWNLOAD_BUTTON_TAG


            layoutParams =
                LinearLayout.LayoutParams(
                    playerView.resources
                        .getDimensionPixelSize(
                            Media3R.dimen
                                .exo_small_icon_width
                        ),
                    playerView.resources
                        .getDimensionPixelSize(
                            Media3R.dimen
                                .exo_small_icon_height
                        )
                ).apply {

                    leftMargin =
                        playerView.resources
                            .getDimensionPixelSize(
                                Media3R.dimen
                                    .exo_small_icon_horizontal_margin
                            )

                    rightMargin =
                        playerView.resources
                            .getDimensionPixelSize(
                                Media3R.dimen
                                    .exo_small_icon_horizontal_margin
                            )
                }

            setPadding(
                playerView.resources
                    .getDimensionPixelSize(
                        Media3R.dimen
                            .exo_small_icon_padding_horizontal
                    ),
                playerView.resources
                    .getDimensionPixelSize(
                        Media3R.dimen
                            .exo_small_icon_padding_vertical
                    ),
                playerView.resources
                    .getDimensionPixelSize(
                        Media3R.dimen
                            .exo_small_icon_padding_horizontal
                    ),
                playerView.resources
                    .getDimensionPixelSize(
                        Media3R.dimen
                            .exo_small_icon_padding_vertical
                    )
            )

            scaleType =
                android.widget.ImageView
                    .ScaleType
                    .CENTER_INSIDE


            val typedValue =
                android.util.TypedValue()

            if (
                playerView.context.theme
                    .resolveAttribute(
                        android.R.attr
                            .selectableItemBackground,
                        typedValue,
                        true
                    )
            ) {
                setBackgroundResource(
                    typedValue.resourceId
                )
            }

        }


    val fullscreenButton =
        basicControls.findViewById<ImageButton>(
            Media3R.id.exo_fullscreen
        )

    val fullscreenIndex =
        if (fullscreenButton != null) {
            basicControls.indexOfChild(
                fullscreenButton
            )
        } else {
            basicControls.childCount
        }

    basicControls.addView(
        downloadButton,
        fullscreenIndex
    )

    updateDownloadButton(
        playerView = playerView,
        hlsUrl = hlsUrl,
        title = title,
        downloadState = downloadState
    )
}

@OptIn(UnstableApi::class)
private fun updateDownloadButton(
    playerView: PlayerView,
    hlsUrl: String,
    title: String?,
    downloadState: VideoDownloadStatus
) {
    val basicControls =
        playerView.findViewById<LinearLayout>(
            Media3R.id.exo_basic_controls
        ) ?: return

    val button =
        basicControls.findViewWithTag<ImageButton>(
            DOWNLOAD_BUTTON_TAG
        ) ?: return

    when (downloadState) {

        VideoDownloadStatus.NotDownloaded -> {
            button.setImageResource(
                R.drawable.download
            )

            button.contentDescription =
                "Download video"

            button.isEnabled = true
        }

        VideoDownloadStatus.Downloading -> {
            button.setImageResource(
                R.drawable.download
            )

            button.contentDescription =
                "Downloading video"

            button.isEnabled = false
        }

        VideoDownloadStatus.Downloaded -> {
            button.setImageResource(
                R.drawable.download_done
            )

            button.contentDescription =
                "Remove downloaded video"

            button.isEnabled = true
        }

        VideoDownloadStatus.Failed -> {
            button.setImageResource(
                R.drawable.download
            )

            button.contentDescription =
                "Retry video download"

            button.isEnabled = true
        }
    }

    button.setOnClickListener {
        when (downloadState) {
            VideoDownloadStatus.Downloaded -> {
                LocalVideoDownloadManager.remove(hlsUrl)
            }
            VideoDownloadStatus.Downloading -> {
            }
            VideoDownloadStatus.NotDownloaded,
            VideoDownloadStatus.Failed -> {
                LocalVideoDownloadManager.download(hlsUrl, title)
            }
        }
    }
}

private const val DOWNLOAD_BUTTON_TAG =
    "calmed_download_button"

@Composable
private fun <T> StateFlow<T>.collectAsStateCompat(): State<T> {
    val flow = this

    val state =
        remember(flow) {
            mutableStateOf(flow.value)
        }

    LaunchedEffect(flow) {
        flow.collectLatest {
            state.value = it
        }
    }

    return state
}