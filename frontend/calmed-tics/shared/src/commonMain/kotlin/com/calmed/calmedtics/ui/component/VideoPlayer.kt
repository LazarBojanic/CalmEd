package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class VideoPlaylistItem(
    val url: String,
    val title: String? = null
)

@Composable
expect fun VideoPlayer(
    hlsUrl: String,
    title: String? = null,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    isPlaying: Boolean = true,
    isMuted: Boolean = false,
    useController: Boolean = true,
    showFullscreenButton: Boolean = false,
    showPrevNextButtons: Boolean = false,
    showRewindFastForwardButtons: Boolean = false,
    playlist: List<VideoPlaylistItem>? = null,
    onPositionChanged: ((Long) -> Unit)? = null,
    onDurationChanged: ((Long) -> Unit)? = null,
    onVideoOrientationChanged: ((isPortrait: Boolean) -> Unit)? = null,
    onFullscreenToggle: ((Boolean) -> Unit)? = null,
    onControllerVisibilityChanged: ((Boolean) -> Unit)? = null,
    onPlaybackEnded: (() -> Unit)? = null,
    onPlayPauseChange: ((Boolean) -> Unit)? = null,
    onPlaylistIndexChanged: ((Int) -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    canGoPrevious: Boolean = false,
    canGoNext: Boolean = false,
    repeatCurrentExercise: Boolean = false
)
