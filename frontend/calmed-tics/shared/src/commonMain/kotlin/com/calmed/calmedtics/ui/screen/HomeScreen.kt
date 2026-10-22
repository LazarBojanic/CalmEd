package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.its_not_yet_time_week
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.home.CurrentExerciseCard
import com.calmed.calmedtics.ui.component.home.HomeHeader
import com.calmed.calmedtics.ui.component.home.MorningEveningCheckCards
import com.calmed.calmedtics.ui.component.home.WeekCalendarCard
import com.calmed.calmedtics.util.currentYmd
import com.calmed.calmedtics.util.dateToEpochDay
import com.calmed.calmedtics.viewmodel.ExercisesViewModel
import com.calmed.calmedtics.viewmodel.HomeViewModel
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    homeViewModel: HomeViewModel,
    exercisesViewModel: ExercisesViewModel,
    onExerciseClick: (ProgramExerciseDto) -> Unit,
) {
    val home by homeViewModel.home.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val user by sessionViewModel.user.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val userInfo by sessionViewModel.userInfo.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val allExercises by exercisesViewModel.exercises.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val allCompletions by homeViewModel.allCompletions.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

    val ymd = currentYmd()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userId = user?.id ?: ""
    val todayEpoch = remember(ymd) { dateToEpochDay(ymd.year, ymd.month, ymd.day) }

    val startWeekday = remember(home?.programStartDate, user?.createdAt) {
        val startDate = home?.programStartDate ?: user?.createdAt ?: ""
        if (startDate.isBlank()) {
            1
        } else {
            try {
                val parts = startDate.substring(0, 10).split("-")
                val startYear = parts[0].toInt()
                val startMonth = parts[1].toInt()
                val startDay = parts[2].toInt()
                val startEpoch = dateToEpochDay(startYear, startMonth, startDay)
                ((startEpoch + 3) % 7).toInt()
            } catch (e: Exception) {
                1
            }
        }
    }

    val activeWeek = home?.currentWeek ?: 1

    var trackerStartEpoch by remember(home?.programStartDate, user?.createdAt, startWeekday, activeWeek) {
        val startDate = home?.programStartDate ?: user?.createdAt ?: ""
        val epoch = if (startDate.isBlank()) {
            0L
        } else {
            try {
                val parts = startDate.substring(0, 10).split("-")
                val startYear = parts[0].toInt()
                val startMonth = parts[1].toInt()
                val startDay = parts[2].toInt()
                val startEpoch = dateToEpochDay(startYear, startMonth, startDay)
                val startWeekMondayEpoch = startEpoch - ((startEpoch + 3) % 7 - startWeekday + 7) % 7
                startWeekMondayEpoch + (activeWeek - 1) * 7
            } catch (e: Exception) {
                0L
            }
        }
        mutableStateOf(epoch)
    }

    var selectedTrackerDateEpoch by remember { mutableStateOf(todayEpoch) }

    val displayWeek = remember(
        trackerStartEpoch,
        home?.programStartDate,
        user?.createdAt,
        startWeekday,
        activeWeek
    ) {
        val startDate = home?.programStartDate ?: user?.createdAt ?: ""
        if (startDate.isBlank()) {
            activeWeek
        } else {
            try {
                val parts = startDate.substring(0, 10).split("-")
                val startYear = parts[0].toInt()
                val startMonth = parts[1].toInt()
                val startDay = parts[2].toInt()
                val startEpoch = dateToEpochDay(startYear, startMonth, startDay)
                val startWeekMondayEpoch =
                    startEpoch - ((startEpoch + 3) % 7 - startWeekday + 7) % 7
                val diff = trackerStartEpoch - startWeekMondayEpoch
                (diff / 7).toInt() + 1
            } catch (e: Exception) {
                activeWeek
            }
        }
    }

    val startEpoch = remember(
        home?.programStartDate,
        user?.createdAt
    ) {
        val startDate = home?.programStartDate ?: user?.createdAt ?: ""
        if (startDate.isNotBlank()) {
            try {
                val parts = startDate.substring(0, 10).split("-")
                dateToEpochDay(
                    parts[0].toInt(),
                    parts[1].toInt(),
                    parts[2].toInt()
                )
            } catch (e: Exception) {
                0L
            }
        } else {
            0L
        }
    }

    val startWeekMondayEpoch = remember(startEpoch, startWeekday) {
        if (startEpoch == 0L) {
            0L
        } else {
            startEpoch - ((startEpoch + 3) % 7 - startWeekday + 7) % 7
        }
    }

    val selectedDayInWeek = remember(selectedTrackerDateEpoch, trackerStartEpoch) {
        ((selectedTrackerDateEpoch - trackerStartEpoch).toInt() + 1).coerceIn(1, 7)
    }

    val completions = remember(allCompletions, selectedDayInWeek, userId, displayWeek) {
        allCompletions.filter { it.userId == userId && it.day == selectedDayInWeek && it.week == displayWeek }
    }

    val morningCompleted = completions.any {
        it.session.equals("MORNING", ignoreCase = true) && it.completed
    }

    val eveningCompleted = completions.any {
        it.session.equals("EVENING", ignoreCase = true) && it.completed
    }

    val weekExercises = remember(allExercises, displayWeek) {
        allExercises.filter { it.weekNumber == displayWeek }
    }
    val selectedExercise = remember(weekExercises) { weekExercises.firstOrNull() }

    val itsNotYetTimeMessage = stringResource(Res.string.its_not_yet_time_week)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundGradient())
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 18.dp,
                end = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeHeader(
                    preferredName = userInfo?.preferredName,
                    username = user?.username
                )
            }

            item {
                WeekCalendarCard(
                    displayWeek = displayWeek,
                    trackerStartEpoch = trackerStartEpoch,
                    startWeekMondayEpoch = startWeekMondayEpoch,
                    currentProgramWeek = home?.currentWeek ?: 1,
                    selectedTrackerDateEpoch = selectedTrackerDateEpoch,
                    todayEpoch = todayEpoch,
                    startWeekday = startWeekday,
                    allCompletions = allCompletions,
                    userId = userId,
                    onPreviousWeek = {
                        trackerStartEpoch -= 7
                    },
                    onNextWeek = {
                        if (displayWeek < (home?.currentWeek ?: 1)) {
                            trackerStartEpoch += 7
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(itsNotYetTimeMessage)
                            }
                        }
                    },
                    onSelectDate = { epoch ->
                        selectedTrackerDateEpoch = epoch
                    }
                )
            }

            item {
                MorningEveningCheckCards(
                    morningCompleted = morningCompleted,
                    eveningCompleted = eveningCompleted,
                    onToggleMorning = {
                        scope.launch {
                            homeViewModel.markSessionCompleted(
                                displayWeek,
                                selectedDayInWeek,
                                userId,
                                "MORNING",
                                !morningCompleted
                            )
                        }
                    },
                    onToggleEvening = {
                        scope.launch {
                            homeViewModel.markSessionCompleted(
                                displayWeek,
                                selectedDayInWeek,
                                userId,
                                "EVENING",
                                !eveningCompleted
                            )
                        }
                    }
                )
            }

            item {
                CurrentExerciseCard(
                    exercise = selectedExercise,
                    displayWeek = displayWeek,
                    onClick = {
                        selectedExercise?.let { exercise ->
                            onExerciseClick(exercise)
                        }
                    }
                )
            }
        }
    }
}
