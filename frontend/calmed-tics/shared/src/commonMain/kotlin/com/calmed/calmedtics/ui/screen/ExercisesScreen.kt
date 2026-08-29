package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.its_not_yet_time
import calmedtics.shared.generated.resources.locked
import calmedtics.shared.generated.resources.week_title
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.ThumbnailImage
import com.calmed.calmedtics.ui.component.VideoPlayerDownloadButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExercisesScreen(
    currentWeek: Int,
    exercises: List<ProgramExerciseDto>,
    onExerciseClick: (ProgramExerciseDto) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val grouped = remember(exercises) {
        exercises
            .sortedWith(
                compareBy(
                    { it.weekNumber }
                )
            )
            .groupBy { it.weekNumber }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundGradient())
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                grouped.forEach { (week, weekItems) ->

                    item {
                        Text(
                            text = stringResource(
                                Res.string.week_title,
                                week
                            ),
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color =
                                if (week > currentWeek) {
                                    MaterialTheme.colorScheme.outline
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                        )
                    }

                    items(weekItems) { ex ->

                        val locked = week > currentWeek

                        val lockedMessage =
                            stringResource(
                                Res.string.its_not_yet_time
                            )

                        val formattedDuration =
                            ex.durationSeconds?.let { totalSeconds ->
                                val minutes = totalSeconds / 60
                                val seconds = totalSeconds % 60

                                "${minutes}:${
                                    seconds
                                        .toString()
                                        .padStart(2, '0')
                                } min"
                            } ?: "--:-- min"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                                .alpha(
                                    if (locked) 0.5f
                                    else 1f
                                )
                                .clickable {
                                    if (locked) {
                                        scope.launch {
                                            snackbarHostState
                                                .showSnackbar(
                                                    lockedMessage
                                                )
                                        }
                                    } else {
                                        onExerciseClick(ex)
                                    }
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 5.dp
                            )
                        ) {

                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                if (!ex.thumbnailURL.isNullOrBlank()) {

                                    ThumbnailImage(
                                        url = ex.thumbnailURL ?: "",
                                        contentDescription = ex.title,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                } else {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                MaterialTheme
                                                    .colorScheme
                                                    .surfaceVariant
                                            ),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {
                                        Text(
                                            text = "No image",
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurface
                                                        .copy(alpha = 0.68f),

                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurface
                                                        .copy(alpha = 0.30f),

                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                ex.videoURL
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { videoUrl ->
                                        VideoPlayerDownloadButton(
                                            hlsUrl = videoUrl,
                                            title = ex.title,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                        )
                                    }

                                Column(
                                    modifier = Modifier
                                        .align(
                                            Alignment.BottomStart
                                        )
                                        .fillMaxWidth(0.65f)
                                        .padding(
                                            start = 18.dp,
                                            end = 8.dp,
                                            bottom = 16.dp
                                        ),
                                    verticalArrangement =
                                        Arrangement.spacedBy(6.dp)
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush =
                                                    Brush.horizontalGradient(
                                                        colors =
                                                            listOf(
                                                                MaterialTheme
                                                                    .colorScheme
                                                                    .primary,

                                                                MaterialTheme
                                                                    .colorScheme
                                                                    .secondary
                                                            )
                                                    ),
                                                shape =
                                                    RoundedCornerShape(
                                                        9.dp
                                                    )
                                            )
                                            .padding(
                                                horizontal = 9.dp,
                                                vertical = 4.dp
                                            )
                                    ) {
                                        Text(
                                            text = "WEEK $week",
                                            fontSize = 9.sp,
                                            fontWeight =
                                                FontWeight.Bold,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onPrimary
                                        )
                                    }

                                    Text(
                                        text = ex.title,
                                        fontSize = 19.sp,
                                        lineHeight = 22.sp,
                                        fontWeight =
                                            FontWeight.Bold,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurface,
                                        maxLines = 2,
                                        overflow =
                                            TextOverflow.Ellipsis
                                    )

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(7.dp)
                                    ) {

                                        Text(
                                            text = formattedDuration,
                                            fontSize = 12.sp,
                                            fontWeight =
                                                FontWeight.Medium,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurface
                                                    .copy(alpha = 0.90f)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurface
                                                        .copy(alpha = 0.75f),
                                                    CircleShape
                                                )
                                        )

                                        Text(
                                            text = "Week $week",
                                            fontSize = 12.sp,
                                            fontWeight =
                                                FontWeight.Medium,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurface
                                                    .copy(alpha = 0.90f)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(52.dp)
                                        .background(
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .surface
                                                    .copy(alpha = 0.95f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Icon(
                                        imageVector =
                                            if (locked) {
                                                Icons.Default.Lock
                                            } else {
                                                Icons.Default.PlayArrow
                                            },
                                        contentDescription =
                                            if (locked) {
                                                stringResource(
                                                    Res.string.locked
                                                )
                                            } else {
                                                "Play"
                                            },
                                        tint =
                                            MaterialTheme
                                                .colorScheme
                                                .primary,
                                        modifier =
                                            Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}