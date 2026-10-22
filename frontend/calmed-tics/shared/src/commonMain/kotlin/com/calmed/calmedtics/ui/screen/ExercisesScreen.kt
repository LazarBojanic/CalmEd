package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.model.dto.response.ExerciseGroupDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.exercises.ExerciseCard
import com.calmed.calmedtics.ui.component.exercises.GroupHeader
import com.calmed.calmedtics.ui.component.exercises.groupAccent
import com.calmed.calmedtics.ui.component.exercises.groupIdForWeek
import com.calmed.calmedtics.ui.component.exercises.isGroupLocked
import kotlinx.coroutines.launch

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
                    .padding(bottom = 80.dp)
            ) {
                orderedGroups.forEach { group ->
                    val groupItems = byGroup[group.id].orEmpty()
                    val groupLocked = isGroupLocked(groupItems, currentWeek)
                    val isExpanded = expanded[group.id] == true

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
                                onLockedClick = { message ->
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                },
                                onExerciseClick = onExerciseClick
                            )
                        }
                    }
                }
            }
        }
    }
}
