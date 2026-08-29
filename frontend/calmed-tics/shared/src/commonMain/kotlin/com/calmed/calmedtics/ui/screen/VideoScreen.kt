package com.calmed.calmedtics.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.exercise_counter
import calmedtics.shared.generated.resources.mobile_rotate
import calmedtics.shared.generated.resources.no_exercises_available
import calmedtics.shared.generated.resources.turn_your_phone
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.BackButton
import com.calmed.calmedtics.ui.component.CastButton
import com.calmed.calmedtics.ui.component.FullscreenEffect
import com.calmed.calmedtics.ui.component.KeepScreenAwake
import com.calmed.calmedtics.ui.component.PlatformBackHandler
import com.calmed.calmedtics.ui.component.PlayerTopOverlayInset
import com.calmed.calmedtics.ui.component.VideoOverlayButton
import com.calmed.calmedtics.ui.component.VideoPlayer
import com.calmed.calmedtics.ui.component.VideoPlayerDownloadButton
import com.calmed.calmedtics.ui.component.VideoPlaylistItem
import com.calmed.calmedtics.video.VideoResolution
import com.calmed.calmedtics.video.applyMaxResolution
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun VideoScreen(
    exercises: List<ProgramExerciseDto>,
    startIndex: Int = 0,
    currentWeek: Int,
    onBack: () -> Unit
) {
    val safeStartIndex = if (exercises.isNotEmpty()) {
        val requestedIndex = startIndex.coerceIn(0, exercises.lastIndex)

        if (exercises[requestedIndex].weekNumber <= currentWeek) {
            requestedIndex
        } else {
            exercises.indexOfLast {
                it.weekNumber <= currentWeek
            }.takeIf { it >= 0 } ?: 0
        }
    } else {
        0
    }

    var currentIndex by remember(exercises, startIndex, currentWeek) {
        mutableStateOf(safeStartIndex)
    }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }

    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var isVideoPortrait by remember { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }

    var showSettings by remember { mutableStateOf(false) }
    var showResolutionPicker by remember { mutableStateOf(false) }
    var selectedResolution by rememberSaveable { mutableStateOf(VideoResolution.R1080) }
    var autoPlayNext by rememberSaveable { mutableStateOf(false) }
    var keepScreenAwake by rememberSaveable { mutableStateOf(false) }
    var repeatCurrentExercise by rememberSaveable { mutableStateOf(false) }
    var restartTrigger by remember { mutableStateOf(0) }

    PlatformBackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    FullscreenEffect(
        isFullscreen = isFullscreen,
        isVideoPortrait = isVideoPortrait,
        onDeviceOrientationChanged = { isLandscape ->
            if (isLandscape && !isVideoPortrait) {
                isFullscreen = true
            } else if (!isLandscape && !isVideoPortrait && isFullscreen) {
                isFullscreen = false
            }
        }
    )

    KeepScreenAwake(enabled = keepScreenAwake)

    if (exercises.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundGradient())
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            BackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
            Text(
                text = stringResource(Res.string.no_exercises_available),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )
        }
        return
    }

    val currentExercise = exercises[currentIndex]
    val currentUrl = currentExercise.videoURL ?: ""
    val displayTitle = currentExercise.title
    val resolvedUrl = remember(currentUrl, selectedResolution) {
        applyMaxResolution(currentUrl, selectedResolution)
    }
    val canGoNext =
        currentIndex < exercises.lastIndex &&
                exercises[currentIndex + 1].weekNumber <= currentWeek

    val canGoPrevious = currentIndex > 0

    val playlistItems =
        remember(exercises, selectedResolution) {
            exercises.map { exercise ->
                VideoPlaylistItem(
                    url = applyMaxResolution(
                        exercise.videoURL ?: "",
                        selectedResolution
                    ),
                    title = exercise.title
                )
            }
        }

    LaunchedEffect(currentIndex) {
        isPlaying = true
    }

    val onPlaybackEndedHandler: () -> Unit = {
        if (repeatCurrentExercise) {
            restartTrigger++
        } else if (autoPlayNext && canGoNext) {
            currentIndex++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isFullscreen) Modifier else Modifier.statusBarsPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = if (isFullscreen) {
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 56.dp)
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                }
            ) {
                VideoPlayer(
                    hlsUrl = resolvedUrl,
                    title = displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    isFullscreen = isFullscreen,
                    isPlaying = isPlaying,
                    isMuted = isMuted,
                    useController = true,
                    showFullscreenButton = true,
                    showPrevNextButtons = true,
                    showRewindFastForwardButtons = true,
                    playlist = playlistItems,
                    onVideoOrientationChanged = { isPortrait ->
                        isVideoPortrait = isPortrait
                    },
                    onFullscreenToggle = { shouldBeFullscreen ->
                        isFullscreen = shouldBeFullscreen
                    },
                    onControllerVisibilityChanged = { visible ->
                        controlsVisible = visible
                    },
                    onPlaybackEnded = onPlaybackEndedHandler,
                    onPlayPauseChange = { isPlaying = it },
                    onPlaylistIndexChanged = { index ->
                        if (index in exercises.indices) {
                            currentIndex = index
                        }
                    },
                    onPrevious = {
                        if (currentIndex > 0) currentIndex--
                    },
                    onNext = {
                        if (canGoNext) currentIndex++
                    },
                    canGoPrevious = canGoPrevious,
                    canGoNext = canGoNext,
                    autoPlayNext = autoPlayNext,
                    repeatCurrentExercise = repeatCurrentExercise,
                    restartTrigger = restartTrigger
                )

                Crossfade(
                    targetState = controlsVisible,
                    modifier = Modifier.align(Alignment.TopEnd),
                    label = "overlayVisibility"
                ) { visible ->
                    if (visible) {
                        Column(
                            modifier = Modifier.padding(
                                start = 8.dp,
                                top = PlayerTopOverlayInset,
                                end = 8.dp,
                                bottom = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VideoPlayerDownloadButton(
                                hlsUrl = currentUrl,
                                title = displayTitle
                            )

                            CastButton(
                                hlsUrl = currentUrl,
                                title = displayTitle,
                                modifier = Modifier.size(28.dp)
                            )

                            VideoOverlayButton(
                                icon = Icons.Default.Hd,
                                contentDescription = "Resolution",
                                onClick = { showResolutionPicker = true }
                            )

                            VideoOverlayButton(
                                icon = Icons.Default.Settings,
                                contentDescription = "Settings",
                                onClick = { showSettings = true }
                            )

                            VideoOverlayButton(
                                icon = if (isMuted) {
                                    Icons.AutoMirrored.Filled.VolumeOff
                                } else {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                },
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                onClick = { isMuted = !isMuted }
                            )
                        }
                    }
                }
            }

            if (!isFullscreen) {
                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.mobile_rotate),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(84.dp)
                    )

                    Text(
                        text = stringResource(Res.string.turn_your_phone),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(Res.string.exercise_counter, currentIndex + 1, exercises.size),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (!isFullscreen) {
            BackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 8.dp)
            )
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Close")
                }
            },
            title = {
                Text("Settings")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-play next exercise")
                        Switch(
                            checked = autoPlayNext,
                            onCheckedChange = { autoPlayNext = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Keep screen awake")
                        Switch(
                            checked = keepScreenAwake,
                            onCheckedChange = { keepScreenAwake = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Repeat current exercise")
                        Switch(
                            checked = repeatCurrentExercise,
                            onCheckedChange = { repeatCurrentExercise = it }
                        )
                    }
                }
            }
        )
    }

    if (showResolutionPicker) {
        AlertDialog(
            onDismissRequest = { showResolutionPicker = false },
            confirmButton = {
                TextButton(onClick = { showResolutionPicker = false }) {
                    Text("Close")
                }
            },
            title = {
                Text("Resolution")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VideoResolution.entries.forEach { resolution ->
                        ResolutionOptionRow(
                            label = resolution.label,
                            selected = selectedResolution == resolution,
                            onClick = {
                                selectedResolution = resolution
                                showResolutionPicker = false
                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun ResolutionOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            }
        )
    }
}
