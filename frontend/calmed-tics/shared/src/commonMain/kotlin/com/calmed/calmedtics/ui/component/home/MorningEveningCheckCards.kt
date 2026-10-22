package com.calmed.calmedtics.ui.component.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.completed
import calmedtics.shared.generated.resources.evening
import calmedtics.shared.generated.resources.home_great_start
import calmedtics.shared.generated.resources.home_keep_going
import calmedtics.shared.generated.resources.home_start_your_day
import calmedtics.shared.generated.resources.home_well_done
import calmedtics.shared.generated.resources.morning
import calmedtics.shared.generated.resources.pending
import com.calmed.calmedtics.theme.blue008
import com.calmed.calmedtics.theme.blue009
import com.calmed.calmedtics.theme.orange008
import com.calmed.calmedtics.theme.purple010
import com.calmed.calmedtics.theme.yellow007
import org.jetbrains.compose.resources.stringResource

@Composable
fun MorningEveningCheckCards(
    morningCompleted: Boolean,
    eveningCompleted: Boolean,
    onToggleMorning: () -> Unit,
    onToggleEvening: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onToggleMorning() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 13.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = orange008,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☀",
                            fontSize = 23.sp,
                            color = yellow007
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.morning),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = if (morningCompleted) {
                                    stringResource(Res.string.completed)
                                } else {
                                    stringResource(Res.string.pending)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (morningCompleted) {
                                    yellow007
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .then(
                                        if (morningCompleted) {
                                            Modifier.background(
                                                color = orange008,
                                                shape = CircleShape
                                            )
                                        } else {
                                            Modifier.border(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.outline,
                                                shape = CircleShape
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (morningCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = yellow007
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(11.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (morningCompleted) {
                        stringResource(Res.string.home_great_start)
                    } else {
                        stringResource(Res.string.home_start_your_day)
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onToggleEvening() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 13.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = purple010,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☾",
                            fontSize = 23.sp,
                            color = blue008
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.evening),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = if (eveningCompleted) {
                                    stringResource(Res.string.completed)
                                } else {
                                    stringResource(Res.string.pending)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (eveningCompleted) {
                                    blue008
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .then(
                                        if (eveningCompleted) {
                                            Modifier.background(
                                                color = purple010,
                                                shape = CircleShape
                                            )
                                        } else {
                                            Modifier.border(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.outline,
                                                shape = CircleShape
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (eveningCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = blue008
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(11.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (eveningCompleted) {
                        stringResource(Res.string.home_well_done)
                    } else {
                        stringResource(Res.string.home_keep_going)
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
