package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.calmed.calmedtics.exercise_locked_dialog_message
import com.calmed.calmedtics.i_am_sure
import com.calmed.calmedtics.cancel
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.its_not_yet_time
import com.calmed.calmedtics.locked
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.ui.component.ThumbnailImage
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import com.calmed.calmedtics.week_title
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import com.calmed.calmedtics.theme.appBackgroundGradient

@Composable
fun ExercisesScreen(
    currentWeek: Int,
    exercises: List<ProgramExerciseDto>,
    onExerciseClick: (ProgramExerciseDto) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedLockedExercise by remember { mutableStateOf<ProgramExerciseDto?>(null) }

    val grouped = remember(exercises) {
        exercises
            .sortedWith(compareBy({ it.weekNumber }, { it.orderInWeek ?: 0 }))
            .groupBy { it.weekNumber }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(modifier = Modifier.fillMaxSize().background(appBackgroundGradient())) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                grouped.forEach { (week, weekItems) ->
                    item {
                        Text(
                            text = stringResource(Res.string.week_title, week),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (week > currentWeek) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                        )
                    }

                    items(weekItems) { ex ->
                        val locked = week > currentWeek
                        val lockedMessage = stringResource(Res.string.its_not_yet_time)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .alpha(if (locked) 0.5f else 1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(horizontal = 16.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        if (locked) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(lockedMessage)
                                            }
                                        } else {
                                            onExerciseClick(ex)
                                        }
                                    }
                            ) {
                                ThumbnailImage(
                                    url = ex.thumbnailURL ?: "",
                                    contentDescription = ex.title,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (locked) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = stringResource(Res.string.locked),
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayTitle = ex.title

                                Text(
                                    text = displayTitle,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}