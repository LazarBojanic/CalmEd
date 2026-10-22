package com.calmed.calmedtics.ui.component.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.duration_min
import calmedtics.shared.generated.resources.duration_placeholder
import calmedtics.shared.generated.resources.home_current_exercise
import calmedtics.shared.generated.resources.no_image
import calmedtics.shared.generated.resources.select_video
import calmedtics.shared.generated.resources.week_number
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.ui.component.Thumbnail
import com.calmed.calmedtics.ui.component.VideoPlayerDownloadButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun CurrentExerciseCard(
    exercise: ProgramExerciseDto?,
    displayWeek: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbnailUrl = exercise?.thumbnailURL
    val title = exercise?.title

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable(enabled = exercise != null, onClick = onClick),
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
            if (!thumbnailUrl.isNullOrBlank()) {
                Thumbnail(
                    url = thumbnailUrl,
                    contentDescription = title ?: "",
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
                        text = stringResource(Res.string.no_image),
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

            exercise
                ?.takeIf { it.playbackId.isNotBlank() }
                ?.let { ex ->
                    VideoPlayerDownloadButton(
                        exercise = ex,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    )
                }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.60f)
                    .padding(
                        start = 18.dp,
                        end = 8.dp,
                        bottom = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(9.dp)
                        )
                        .padding(
                            horizontal = 9.dp,
                            vertical = 4.dp
                        )
                ) {
                    Text(
                        text = stringResource(Res.string.home_current_exercise),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = title.orEmpty(),
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
                        text = exercise?.durationSeconds?.let { totalSeconds ->
                            val minutes = totalSeconds / 60
                            val seconds = totalSeconds % 60

                            stringResource(
                                Res.string.duration_min,
                                "$minutes:${seconds.toString().padStart(2, '0')}"
                            )
                        } ?: stringResource(Res.string.duration_placeholder),
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
                        text = stringResource(Res.string.week_number, displayWeek),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)
                    )
                }
            }

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
                    contentDescription = stringResource(Res.string.select_video),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(start = 2.dp)
                )
            }
        }
    }
}
