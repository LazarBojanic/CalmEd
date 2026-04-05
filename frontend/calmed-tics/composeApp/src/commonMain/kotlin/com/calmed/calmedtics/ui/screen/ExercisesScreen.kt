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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.exercise_locked_message
import com.calmed.calmedtics.locked
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.ui.component.ThumbnailImage
import com.calmed.calmedtics.util.getTitle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import com.calmed.calmedtics.week_title


@Composable
fun ExercisesScreen(
    currentWeek: Int,
    exercises: List<ProgramExerciseDto>,
    onExerciseClick: (ProgramExerciseDto) -> Unit,
    language: String
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val grouped = remember(exercises) {
        exercises
            .sortedWith(compareBy({ it.weekNumber }, { it.orderInWeek ?: 0 }))
            .groupBy { it.weekNumber }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {

            grouped.forEach { (week, weekItems) ->

                item {
                    Text(
                        text = stringResource(Res.string.week_title, week),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(weekItems) { ex ->
                    val locked = ex.weekNumber > currentWeek
                    val lockedMessage = stringResource(Res.string.exercise_locked_message)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 16.dp)
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
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (locked) 0.5f else 1f)
                        )

                        if (locked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(Res.string.locked),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .alpha(if (locked) 0.6f else 1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (locked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(text = ex.getTitle(language))
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}