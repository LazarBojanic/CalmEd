package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.calmed.calmedtics.ui.component.VideoPlayerWithState
import com.calmed.calmedtics.util.getTitle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.Switch

private val ScreenTop = Color(0xFFC7BCFF)
private val ScreenBottom = Color(0xFFE8E1F6)
private val PanelColor = Color(0xFFD4CBF3)
private val TitleBarColor = Color(0xFFD0C6FA)
private val PrimaryPurpleDark = Color(0xFF6C4BD2)
private val SoftWhite = Color(0xFFF4EFFB)

@Composable
fun FullscreenVideoScreen(
    exercises: List<ProgramExerciseDto>,
    startIndex: Int,
    language: String,
    onBack: () -> Unit
) {
    var currentIndex by remember {
        mutableStateOf(
            if (exercises.isNotEmpty()) startIndex.coerceIn(0, exercises.lastIndex) else 0
        )
    }
    var isPlaying by remember { mutableStateOf(true) }

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var showExerciseInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var autoPlayNext by remember { mutableStateOf(false) }
    var keepScreenAwake by remember { mutableStateOf(false) }
    var repeatCurrentExercise by remember { mutableStateOf(false) }
    var restartTrigger by remember { mutableStateOf(0) }

    if (exercises.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.no_exercises_available),
                color = Color.White,
                fontSize = 18.sp
            )
        }
        return
    }

    val currentExercise = exercises[currentIndex]
    println("DESCRIPTION: ${currentExercise.description}")
    val currentUrl = currentExercise.videoURL ?: ""
    val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)

    val displayTitle = remember(currentExercise, language) {
        currentExercise.getTitle(language)
    }

    LaunchedEffect(currentIndex) {
        currentPositionMs = 0L
        durationMs = 0L
        isPlaying = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ScreenTop, ScreenBottom)
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(92.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(PanelColor)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            .background(Color.White)
                    ) {
                        key(currentUrl) {
                            VideoPlayerWithState(
                                hlsUrl = currentUrl,
                                modifier = Modifier.fillMaxSize(),
                                isPlaying = isPlaying,
                                onPositionChanged = { position ->
                                    currentPositionMs = position

                                    if (
                                        durationMs > 0L &&
                                        position >= durationMs - 500L
                                    ) {
                                        if (repeatCurrentExercise) {
                                            restartTrigger++
                                        } else if (
                                            autoPlayNext &&
                                            currentIndex < exercises.lastIndex
                                        ) {
                                            currentIndex++
                                        }
                                    }
                                },
                                onDurationChanged = { durationMs = it },
                                restartTrigger = restartTrigger

                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 6.dp, top = 6.dp) // bliže ivici
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f)), // malo jače
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ){
                            OverlayIcon(Icons.Default.Fullscreen, onClick = { })
                            OverlayIcon(Icons.Default.Cast, onClick = { })
                            OverlayIcon(Icons.Default.Settings, onClick = { showSettings = true })
                            OverlayIcon(Icons.Default.MusicNote, onClick = { })
                            OverlayIcon(Icons.Default.Info, onClick = { showExerciseInfo = true })
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TitleBarColor)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayTitle,
                            color = Color(0xFF2B2F7E),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9E92F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "?",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = formatTimeFromMillis(remainingMs),
                color = SoftWhite,
                fontSize = 60.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            PlayerControls(
                isPlaying = isPlaying,
                canGoPrevious = currentIndex > 0,
                canGoNext = currentIndex < exercises.lastIndex,
                onPrevious = {
                    if (currentIndex > 0) currentIndex--
                },
                onPlayPause = {
                    isPlaying = !isPlaying
                },
                onNext = {
                    if (currentIndex < exercises.lastIndex) currentIndex++
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(Res.string.exercise_counter, currentIndex + 1, exercises.size),
                color = Color(0xFF4A5BFF),
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
                        TextButton(
                            onClick = { showSettings = false }
                        ) {
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
            .background(Color(0xFFF2ECF7)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp))
                .background(Color(0xFF9C92F6)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = canGoPrevious
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = null,
                    tint = if (canGoPrevious) PrimaryPurpleDark else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onPlayPause
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = PrimaryPurpleDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .width(64.dp)
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onNext,
                enabled = canGoNext
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = if (canGoNext) PrimaryPurpleDark else PrimaryPurpleDark.copy(alpha = 0.35f),
                    modifier = Modifier.size(30.dp)
                )
            }
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
private fun OverlayIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
    }
}