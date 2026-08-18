package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.exercise_counter
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.no_exercises_available
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.CastButton
import com.calmed.calmedtics.ui.component.FullscreenEffect
import com.calmed.calmedtics.ui.component.KeepScreenAwake
import com.calmed.calmedtics.ui.component.PlatformBackHandler
import com.calmed.calmedtics.ui.component.VideoPlayer
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

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var isVideoPortrait by remember { mutableStateOf(true) }

    var showExerciseInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
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
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
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
    val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)
    val displayTitle = currentExercise.title
    val canGoNext =
        currentIndex < exercises.lastIndex &&
                exercises[currentIndex + 1].weekNumber <= currentWeek

    LaunchedEffect(currentIndex) {
        currentPositionMs = 0L
        durationMs = 0L
        isPlaying = true
    }

    val onPlaybackEndedHandler: () -> Unit = {
        if (repeatCurrentExercise) {
            restartTrigger++
        } else if (autoPlayNext && canGoNext) {
            currentIndex++
        }
    }

    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
        ) {
            key(currentUrl) {
                VideoPlayer(
                    hlsUrl = currentUrl,
                    modifier = Modifier.fillMaxSize(),
                    isFullscreen = true,
                    isPlaying = isPlaying,
                    isMuted = isMuted,
                    useController = true,
                    onPositionChanged = { position ->
                        currentPositionMs = position
                        if (durationMs > 0L && position >= durationMs - 500L) {
                            onPlaybackEndedHandler()
                        }
                    },
                    onDurationChanged = { durationMs = it },
                    onVideoOrientationChanged = { isPortrait ->
                        isVideoPortrait = isPortrait
                    },
                    onFullscreenToggle = { shouldBeFullscreen ->
                        isFullscreen = shouldBeFullscreen
                    },
                    onPlaybackEnded = onPlaybackEndedHandler,
                    restartTrigger = restartTrigger
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {


                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CastButton(
                        modifier = Modifier.size(32.dp)
                    )


                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundGradient())
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            key(currentUrl) {
                                VideoPlayer(
                                    hlsUrl = currentUrl,
                                    modifier = Modifier.fillMaxSize(),
                                    isFullscreen = false,
                                    isPlaying = isPlaying,
                                    isMuted = isMuted,
                                    useController = true,
                                    onPositionChanged = { position ->
                                        currentPositionMs = position
                                        if (durationMs > 0L && position >= durationMs - 500L) {
                                            onPlaybackEndedHandler()
                                        }
                                    },
                                    onDurationChanged = { durationMs = it },
                                    onVideoOrientationChanged = { isPortrait ->
                                        isVideoPortrait = isPortrait
                                    },
                                    onFullscreenToggle = { shouldBeFullscreen ->
                                        isFullscreen = shouldBeFullscreen
                                    },
                                    onPlaybackEnded = onPlaybackEndedHandler,
                                    restartTrigger = restartTrigger
                                )
                            }

                            OverlayIconButton(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                onClick = onBack,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 12.dp, top = 12.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CastButton(
                                    modifier = Modifier.size(32.dp)
                                )
                                OverlayIconButton(
                                    icon = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    onClick = { showSettings = true }
                                )
                                OverlayIconButton(
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayTitle,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { showExerciseInfo = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "?",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = formatTimeFromMillis(remainingMs),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                PlayerControls(
                    isPlaying = isPlaying,
                    canGoPrevious = currentIndex > 0,
                    canGoNext = canGoNext,
                    onPrevious = {
                        if (currentIndex > 0) currentIndex--
                    },
                    onPlayPause = {
                        isPlaying = !isPlaying
                    },
                    onNext = {
                        if (canGoNext) currentIndex++
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = stringResource(Res.string.exercise_counter, currentIndex + 1, exercises.size),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                if (showExerciseInfo) {
                    AlertDialog(
                        onDismissRequest = { showExerciseInfo = false },
                        confirmButton = {
                            TextButton(onClick = { showExerciseInfo = false }) {
                                Text("Close")
                            }
                        },
                        title = {
                            Text(displayTitle)
                        },
                        text = {
                            Text(
                                currentExercise.description
                                    ?: "No description available"
                            )
                        }
                    )
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
            }
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(210.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(enabled = canGoPrevious, onClick = onPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous Exercise",
                tint = if (canGoPrevious) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f),
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(64.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(enabled = canGoNext, onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next Exercise",
                tint = if (canGoNext) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun formatTimeFromMillis(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).toInt()
    val min = totalSeconds / 60
    val sec = totalSeconds % 60
    return min.toString().padStart(2, '0') + ":" + sec.toString().padStart(2, '0')
}

@Composable
private fun OverlayIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(15.dp)
        )
    }
}
