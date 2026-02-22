package com.calmed.calmedfrontendtourettes.ui.screen

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
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.model.dto.response.ProgramExerciseDto
import com.calmed.calmedfrontendtourettes.ui.component.ThumbnailImage
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch

@Composable
fun ExercisesScreen(
    currentWeek: Int,
    exercises: List<ProgramExerciseDto>,
    client: HttpClient,
    onExerciseClick: (ProgramExerciseDto) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val grouped = exercises
        .sortedWith(compareBy({ it.weekNumber }, { it.orderInWeek ?: 0 }))
        .groupBy { it.weekNumber }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {

            grouped.forEach { (week, weekItems) ->

                item {
                    Text(
                        text = "Week $week",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(weekItems) { ex ->
                    val locked = ex.weekNumber > currentWeek

                    if (locked) {
                        // ✅ LOCK "thumbnail"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = 16.dp)
                                .alpha(0.5f)
                                .clickable {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Još nije vreme za ovu vežbu.")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked"
                            )
                        }
                    } else {
                        // ✅ REAL thumbnail
                        ThumbnailImage(
                            client = client,
                            url = ex.thumbnailURL ?: "",
                            contentDescription = ex.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    onExerciseClick(ex)
                                }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (locked) "🔒 ${ex.title}" else ex.title,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .alpha(if (locked) 0.6f else 1f)
                    )

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}