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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.hourglass
import calmedtics.shared.generated.resources.its_not_yet_time
import calmedtics.shared.generated.resources.upcoming
import com.calmed.calmedtics.model.dto.response.ExerciseGroupDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.ThumbnailImage
import com.calmed.calmedtics.ui.component.VideoPlayerDownloadButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ExercisesScreen(
    currentWeek: Int,
    exercises: List<ProgramExerciseDto>,
    groups: List<ExerciseGroupDto>,
    onExerciseClick: (ProgramExerciseDto) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val byGroup = remember(exercises) {
        exercises.groupBy { it.groupId ?: 0 }
    }

    val orderedGroups = remember(groups) {
        groups.sortedWith(
            compareBy(
                { if (it.id == 0) 1 else 0 },
                { it.id }
            )
        )
    }

    val activeGroupId = groupIdForWeek(currentWeek)

    val expanded = remember(orderedGroups, activeGroupId) {
        mutableStateMapOf<Int, Boolean>().apply {
            orderedGroups.forEach { group ->
                put(group.id, group.id == activeGroupId)
            }
        }
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
                orderedGroups.forEach { group ->
                    val groupItems = byGroup[group.id]
                        .orEmpty()
                        .sortedBy { it.weekNumber }

                    val isExpanded = expanded[group.id] == true
                    val groupLocked = isGroupLocked(groupItems, currentWeek)

                    item(key = "group-header-${group.id}") {
                        GroupHeader(
                            group = group,
                            isExpanded = isExpanded,
                            isLocked = groupLocked,
                            accent = groupAccent(group.id),
                            onClick = {
                                expanded[group.id] = !isExpanded
                            }
                        )
                    }

                    if (isExpanded) {
                        items(groupItems, key = { it.id }) { ex ->
                            ExerciseCard(
                                exercise = ex,
                                currentWeek = currentWeek,
                                snackbarHostState = snackbarHostState,
                                scope = scope,
                                onExerciseClick = onExerciseClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(
    group: ExerciseGroupDto,
    isExpanded: Boolean,
    isLocked: Boolean,
    accent: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(44.dp)
                    .background(accent, RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                group.description?.let { description ->
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLocked) {
                UpcomingChip()
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: ProgramExerciseDto,
    currentWeek: Int,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onExerciseClick: (ProgramExerciseDto) -> Unit
) {
    val locked = exercise.weekNumber > currentWeek

    val lockedMessage = stringResource(Res.string.its_not_yet_time)

    val formattedDuration = exercise.durationSeconds?.let { totalSeconds ->
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        "${minutes}:${seconds.toString().padStart(2, '0')} min"
    } ?: "--:-- min"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(if (locked) 0.5f else 1f)
            .clickable {
                if (locked) {
                    scope.launch {
                        snackbarHostState.showSnackbar(lockedMessage)
                    }
                } else {
                    onExerciseClick(exercise)
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!exercise.thumbnailURL.isNullOrBlank()) {
                ThumbnailImage(
                    url = exercise.thumbnailURL ?: "",
                    contentDescription = exercise.title,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No image",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        )
                    )
            )

            exercise.videoURL
                ?.takeIf { it.isNotBlank() }
                ?.let { videoUrl ->
                    VideoPlayerDownloadButton(
                        hlsUrl = videoUrl,
                        title = exercise.title,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    )
                }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.65f)
                    .padding(start = 18.dp, end = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(9.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "WEEK ${exercise.weekNumber}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = exercise.title,
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = formattedDuration,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)
                    )

                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                CircleShape
                            )
                    )

                    Text(
                        text = "Week ${exercise.weekNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)
                    )
                }
            }

            if (locked) {
                UpcomingChip(modifier = Modifier.align(Alignment.Center))
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.hourglass),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = stringResource(Res.string.upcoming),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun groupAccent(id: Int): Brush {
    val base = when (id) {
        0 -> Color(0xFF9E9E9E)
        1 -> Color(0xFFD32F2F)
        2 -> Color(0xFF1976D2)
        3 -> Color(0xFF388E3C)
        4 -> Color(0xFFF9A825)
        5 -> Color(0xFF8E24AA)
        else -> Color(0xFF9E9E9E)
    }
    return Brush.verticalGradient(
        colors = listOf(base, lerp(base, Color.White, 0.35f))
    )
}

private fun groupIdForWeek(week: Int): Int {
    return when {
        week <= 0 -> 0
        week <= 4 -> 1
        week <= 12 -> 2
        week <= 18 -> 3
        week <= 24 -> 4
        else -> 5
    }
}

private fun isGroupLocked(items: List<ProgramExerciseDto>, currentWeek: Int): Boolean {
    val minWeek = items.minOfOrNull { it.weekNumber } ?: return false
    return minWeek > currentWeek
}
