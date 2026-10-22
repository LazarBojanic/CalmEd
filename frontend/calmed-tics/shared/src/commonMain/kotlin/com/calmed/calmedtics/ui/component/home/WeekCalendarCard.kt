package com.calmed.calmedtics.ui.component.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.next_week
import calmedtics.shared.generated.resources.previous_week
import calmedtics.shared.generated.resources.week_number
import calmedtics.shared.generated.resources.weekday_initials
import com.calmed.calmedtics.model.raw.ExerciseCompletionEntity
import com.calmed.calmedtics.theme.blue009
import com.calmed.calmedtics.theme.yellow003
import com.calmed.calmedtics.theme.yellow007
import com.calmed.calmedtics.util.epochDayToYmd
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WeekCalendarCard(
    displayWeek: Int,
    trackerStartEpoch: Long,
    startWeekMondayEpoch: Long,
    currentProgramWeek: Int,
    selectedTrackerDateEpoch: Long,
    todayEpoch: Long,
    startWeekday: Int,
    allCompletions: List<ExerciseCompletionEntity>,
    userId: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val startYmd = epochDayToYmd(trackerStartEpoch)
    val endYmd = epochDayToYmd(trackerStartEpoch + 6)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp,
                top = 18.dp,
                end = 18.dp,
                bottom = 17.dp
            ),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            ),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(
                            Res.string.week_number,
                            displayWeek
                        ),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${startYmd.day}.${startYmd.month}. – ${endYmd.day}.${endYmd.month}.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = trackerStartEpoch - 7 >= startWeekMondayEpoch
                            ) {
                                onPreviousWeek()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = stringResource(Res.string.previous_week),
                            tint = if (trackerStartEpoch - 7 >= startWeekMondayEpoch) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = CircleShape
                            )
                            .clickable {
                                onNextWeek()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(Res.string.next_week),
                            tint = if (displayWeek < currentProgramWeek) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }

            val daysOfWeekFull = stringArrayResource(Res.array.weekday_initials)
            val daysOfWeek = List(7) { index ->
                daysOfWeekFull[(startWeekday + index) % 7]
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (index in 0..6) {
                    val currentEpoch = trackerStartEpoch + index
                    val currentYmd = epochDayToYmd(currentEpoch)
                    val isSelected = currentEpoch == selectedTrackerDateEpoch
                    val isToday = currentEpoch == todayEpoch
                    val currentDayInWeek = index + 1

                    val dayCompletions = allCompletions.filter {
                        it.userId == userId &&
                                it.day == currentDayInWeek &&
                                it.week == displayWeek &&
                                it.completed
                    }

                    val morningCompleted = dayCompletions.any {
                        it.session.equals("MORNING", ignoreCase = true)
                    }

                    val eveningCompleted = dayCompletions.any {
                        it.session.equals("EVENING", ignoreCase = true)
                    }

                    Column(
                        modifier = Modifier
                            .width(40.dp)
                            .clickable {
                                onSelectDate(currentEpoch)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = daysOfWeek[index],
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    brush = when {
                                        isSelected -> Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                        isToday -> Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                        else -> Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentYmd.day.toString(),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || isToday) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.SemiBold
                                },
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.height(7.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        color = if (morningCompleted) {
                                            yellow007
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        color = if (eveningCompleted) {
                                            blue009
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
