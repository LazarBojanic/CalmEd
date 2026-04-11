package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.calendar_loading
import com.calmed.calmedtics.home_title
import com.calmed.calmedtics.home_welcome
import com.calmed.calmedtics.no_image
import com.calmed.calmedtics.select_video
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.ui.component.ThumbnailImage
import com.calmed.calmedtics.ui.component.VideoPlayer
import com.calmed.calmedtics.util.currentYmd
import com.calmed.calmedtics.util.dateToEpochDay
import com.calmed.calmedtics.util.getTitle
import com.calmed.calmedtics.util.getVideoURL
import com.calmed.calmedtics.localization.LocalAppLocale
import com.calmed.calmedtics.localization.customAppLocale
import com.calmed.calmedtics.localization.resolveContentLanguage
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Instant

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.calmed.calmedtics.exercise_locked_message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import com.calmed.calmedtics.util.epochDayToYmd

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel = koinInject(),
    onOpenFullscreen: (String) -> Unit = {}
) {
    val home by sessionViewModel.home.collectAsState(initial = null)
    val user by sessionViewModel.user.collectAsState()
    val allExercises by sessionViewModel.allExercises.collectAsState()
    val allCompletions by sessionViewModel.allCompletions.collectAsState()
    
    val uiLocaleTag = LocalAppLocale.current
    val contentLanguage = remember(customAppLocale, uiLocaleTag) {
        resolveContentLanguage(customAppLocale, uiLocaleTag)
    }
    val ymd = currentYmd()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedExerciseId by remember { mutableStateOf<String?>(null) }

    val userId = user?.id ?: ""
    
    // Weekly Tracker State
    val todayEpoch = remember(ymd) { dateToEpochDay(ymd.year, ymd.month, ymd.day) }
    
    // Program start weekday calculation (0=Mon, ..., 6=Sun)
    val startWeekday = remember(user?.createdAt) {
        val startDate = user?.createdAt ?: ""
        if (startDate.isNotBlank()) {
            try {
                val parts = startDate.substring(0, 10).split("-")
                val startYear = parts[0].toInt()
                val startMonth = parts[1].toInt()
                val startDay = parts[2].toInt()
                val startEpoch = dateToEpochDay(startYear, startMonth, startDay)
                ((startEpoch + 3) % 7).toInt() // 0=Mon, ..., 6=Sun
            } catch (e: Exception) {
                0 // Default to Monday
            }
        } else {
            0 // Default to Monday
        }
    }

    // Days since the start of the week relative to startWeekday
    // (todayDayOfWeek - startWeekday + 7) % 7
    val todayDayOfWeek = ((todayEpoch + 3) % 7).toInt() // Current weekday (0=Mon, ..., 6=Sun)
    val daysSinceWeekStart = (todayDayOfWeek - startWeekday + 7) % 7
    
    var trackerStartEpoch by remember(todayEpoch, daysSinceWeekStart) { 
        mutableStateOf(todayEpoch - daysSinceWeekStart) 
    }
    
    var selectedTrackerDateEpoch by remember(todayEpoch) { mutableStateOf(todayEpoch) }
    val selectedTrackerYmd = remember(selectedTrackerDateEpoch) { epochDayToYmd(selectedTrackerDateEpoch) }
    val selectedDateStr = "${selectedTrackerYmd.year}-${selectedTrackerYmd.month.toString().padStart(2, '0')}-${selectedTrackerYmd.day.toString().padStart(2, '0')}"

    val completions = remember(allCompletions, selectedDateStr, userId) {
        allCompletions.filter { it.userId == userId && it.date == selectedDateStr }
    }

    LaunchedEffect(Unit) {
        sessionViewModel.loadHome(year = ymd.year, month = ymd.month)
    }

    val activeWeek = home?.currentWeek ?: 1
    
    // We want the exercises to follow the week shown in the tracker, 
    // but constrained by activeWeek for safety (though tracker is now constrained)
    val displayWeek = remember(trackerStartEpoch, user?.createdAt, startWeekday, activeWeek) {
        val startDate = user?.createdAt ?: ""
        if (startDate.isBlank()) {
            activeWeek
        } else {
            try {
                val parts = startDate.substring(0, 10).split("-")
                val startYear = parts[0].toInt()
                val startMonth = parts[1].toInt()
                val startDay = parts[2].toInt()
                val startEpoch = dateToEpochDay(startYear, startMonth, startDay)
                val startWeekMondayEpoch = startEpoch - ((startEpoch + 3) % 7 - startWeekday + 7) % 7
                val diff = trackerStartEpoch - startWeekMondayEpoch
                (diff / 7).toInt() + 1
            } catch (e: Exception) {
                activeWeek
            }
        }
    }

    val weekExercises = remember(allExercises, displayWeek) {
        allExercises.filter { it.weekNumber == displayWeek }.sortedBy { it.orderInWeek ?: Int.MAX_VALUE }
    }
    val displayExercises = if (weekExercises.isNotEmpty()) weekExercises else home?.upNext.orEmpty()
    val selectedExercise = remember(displayExercises, selectedExerciseId) {
        val id = selectedExerciseId
        if (id.isNullOrBlank()) {
            displayExercises.firstOrNull()
        } else {
            displayExercises.firstOrNull { it.id == id } ?: displayExercises.firstOrNull()
        }
    }
    val selectedVideoUrl = remember(selectedExercise, contentLanguage) { selectedExercise?.getVideoURL(contentLanguage) }
    val selectedTitle = remember(selectedExercise, contentLanguage) { selectedExercise?.getTitle(contentLanguage) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(0.dp, 16.dp, 0.dp, 72.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${home?.greetingName ?: "User"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.home_welcome),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { trackerStartEpoch -= 7 }) {
                                Icon(Icons.Default.ChevronLeft, "Previous Week")
                            }
                            
                            val startYmd = epochDayToYmd(trackerStartEpoch)
                            val endYmd = epochDayToYmd(trackerStartEpoch + 6)
                            
                            val maxAllowedWeek = home?.currentWeek ?: 1
                            
                            val weekNumber = remember(trackerStartEpoch, user?.createdAt, startWeekday) {
                                val startDate = user?.createdAt ?: ""
                                if (startDate.isBlank()) {
                                    activeWeek
                                } else {
                                    try {
                                        // user.createdAt is typically ISO format: 2026-04-11T... or 2026-04-11 ...
                                        val parts = startDate.substring(0, 10).split("-")
                                        val startYear = parts[0].toInt()
                                        val startMonth = parts[1].toInt()
                                        val startDay = parts[2].toInt()
                                        val startEpoch = dateToEpochDay(startYear, startMonth, startDay)
                                        // Align startEpoch to the user-defined start of its week
                                        val startWeekMondayEpoch = startEpoch - ((startEpoch + 3) % 7 - startWeekday + 7) % 7
                                        val diff = trackerStartEpoch - startWeekMondayEpoch
                                        (diff / 7).toInt() + 1
                                    } catch (e: Exception) {
                                        activeWeek
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Week $weekNumber",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${startYmd.day}.${startYmd.month} - ${endYmd.day}.${endYmd.month}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            IconButton(onClick = { 
                                if (weekNumber < maxAllowedWeek) {
                                    trackerStartEpoch += 7 
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Not yet time for this week")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight, 
                                    contentDescription = "Next Week",
                                    tint = if (weekNumber < maxAllowedWeek) LocalContentColor.current else Color.Gray
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val daysOfWeekFull = listOf("M", "T", "W", "T", "F", "S", "S")
                            val daysOfWeek = List(7) { i -> daysOfWeekFull[(startWeekday + i) % 7] }
                            for (i in 0..6) {
                                val currentEpoch = trackerStartEpoch + i
                                val currentYmd = epochDayToYmd(currentEpoch)
                                val isSelected = currentEpoch == selectedTrackerDateEpoch
                                val isToday = currentEpoch == todayEpoch
                                
                                val dateStr = "${currentYmd.year}-${currentYmd.month.toString().padStart(2, '0')}-${currentYmd.day.toString().padStart(2, '0')}"
                                val dayCompletions = allCompletions.filter { it.userId == userId && it.date == dateStr }
                                val completedSessionsCount = dayCompletions.map { it.session }.distinct().size
                                
                                val statusColor = when (completedSessionsCount) {
                                    2 -> Color.Green
                                    1 -> Color.Yellow
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedTrackerDateEpoch = currentEpoch }
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = daysOfWeek[i],
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                                CircleShape
                                            )
                                            .border(
                                                width = if (isToday) 2.dp else 1.dp,
                                                color = if (isToday) MaterialTheme.colorScheme.primary else statusColor,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentYmd.day.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        
                                        // Small dot for completion if not selected (or just use the border color)
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 2.dp)
                                                .size(4.dp)
                                                .background(statusColor, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!selectedTitle.isNullOrBlank()) {
                        Text(selectedTitle)
                    }

                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    ) {
                        val url = selectedVideoUrl
                        if (!url.isNullOrBlank()) {
                            key(url) {
                                VideoPlayer(
                                    hlsUrl = url,
                                    modifier = Modifier.fillMaxSize(),
                                    onFullscreenToggle = { onOpenFullscreen(url) }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(Res.string.select_video))
                            }
                        }
                    }
                }
            }

            listItems(displayExercises) { ex ->
                val isSelected = ex.id == selectedExercise?.id
                
                val isWeek0 = ex.weekNumber == 0
                val isMorning = ex.orderInWeek == 1
                val isEvening = ex.orderInWeek == 2
                val sessionType = if (isMorning) "morning" else if (isEvening) "evening" else "morning" // default
                
                val isCompleted = completions.any { comp -> comp.exerciseId == ex.id && comp.session == sessionType }
                
                val displayTitle = if (!isWeek0) {
                    val suffix = if (isMorning) " (Morning)" else if (isEvening) " (Evening)" else ""
                    ex.getTitle(contentLanguage) + suffix
                } else {
                    ex.getTitle(contentLanguage)
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedExerciseId = ex.id
                            }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Card(
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    modifier = Modifier.size(width = 140.dp, height = 80.dp)
                                ) {
                                    val thumb = ex.thumbnailURL
                                    if (!thumb.isNullOrBlank()) {
                                        ThumbnailImage(
                                            url = thumb,
                                            contentDescription = displayTitle,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(stringResource(Res.string.no_image))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))


                                Text(
                                    text = displayTitle,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    style = if (isSelected) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge
                                )
                            }

                            if (!isWeek0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isCompleted,
                                            onCheckedChange = { checked ->
                                                scope.launch {
                                                    sessionViewModel.markExerciseCompleted(ex.id, userId, selectedDateStr, sessionType, checked)
                                                }
                                            }
                                        )
                                        Text(if (isMorning) "Morning" else if (isEvening) "Evening" else "Done", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

private fun epochDayFromMillis(epochMillis: Long): Long {
    return floorDiv(epochMillis, MILLIS_PER_DAY)
}

private fun floorDiv(a: Long, b: Long): Long {
    var r = a / b
    if ((a xor b) < 0 && a % b != 0L) r -= 1
    return r
}

// ISO-8601 to epoch day, same as java.time.LocalDate.toEpochDay
private fun dateToEpochDay(year: Int, month: Int, day: Int): Long {
    var y = year
    val m = month
    y -= if (m <= 2) 1 else 0
    val era = if (y >= 0) y / 400 else (y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097L + doe - 719468L
}
