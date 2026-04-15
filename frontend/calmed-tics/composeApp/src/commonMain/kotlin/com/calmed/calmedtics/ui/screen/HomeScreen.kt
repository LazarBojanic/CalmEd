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
import com.calmed.calmedtics.its_not_yet_time_week

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
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
    
    val todayEpoch = remember(ymd) { dateToEpochDay(ymd.year, ymd.month, ymd.day) }

    val startWeekday = remember(home?.programStartDate, user?.createdAt) {
        val startDate = home?.programStartDate ?: user?.createdAt ?: ""
        if (startDate.isNotBlank()) {
            try {
                val parts = startDate.substring(0, 10).split("-")
                val startYear = parts[0].toInt()
                val startMonth = parts[1].toInt()
                val startDay = parts[2].toInt()
                val startEpoch = dateToEpochDay(startYear, startMonth, startDay)
                ((startEpoch + 3) % 7).toInt()
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }

    val todayDayOfWeek = ((todayEpoch + 3) % 7).toInt()
    val daysSinceWeekStart = (todayDayOfWeek - startWeekday + 7) % 7
    
    val activeWeek = home?.currentWeek ?: 1

    var trackerStartEpoch by remember(todayEpoch, daysSinceWeekStart, activeWeek) { 
        mutableStateOf(todayEpoch - daysSinceWeekStart) 
    }
    
    var selectedTrackerDateEpoch by remember(todayEpoch) { mutableStateOf(todayEpoch) }
    val selectedTrackerYmd = remember(selectedTrackerDateEpoch) { epochDayToYmd(selectedTrackerDateEpoch) }
    val selectedDateStr = "${selectedTrackerYmd.year}-${selectedTrackerYmd.month.toString().padStart(2, '0')}-${selectedTrackerYmd.day.toString().padStart(2, '0')}"

    val displayWeek = remember(trackerStartEpoch, home?.programStartDate, user?.createdAt, startWeekday, activeWeek) {
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
                val startWeekMondayEpoch = startEpoch - ((startEpoch + 3) % 7 - startWeekday + 7) % 7
                val diff = trackerStartEpoch - startWeekMondayEpoch
                (diff / 7).toInt() + 1
            } catch (e: Exception) {
                activeWeek
            }
        }
    }

    val selectedDayInWeek = remember(selectedTrackerDateEpoch, trackerStartEpoch) {
        ((selectedTrackerDateEpoch - trackerStartEpoch).toInt() + 1).coerceIn(1, 7)
    }

    val completions = remember(allCompletions, selectedDayInWeek, userId, displayWeek) {
        allCompletions.filter { it.userId == userId && it.day == selectedDayInWeek && it.week == displayWeek }
    }

    LaunchedEffect(Unit) {
        sessionViewModel.loadHome(year = ymd.year, month = ymd.month)
        sessionViewModel.loadAllExercises()
    }

    val weekExercises = remember(allExercises, displayWeek) {
        allExercises.filter { it.weekNumber == displayWeek }.sortedBy { it.orderInWeek ?: Int.MAX_VALUE }
    }
    val displayExercises = weekExercises.ifEmpty { home?.upNext.orEmpty() }
    val selectedExercise = remember(displayExercises, selectedExerciseId) {
        displayExercises.find { it.id == selectedExerciseId } ?: displayExercises.firstOrNull()
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
                            text = "Hello, ${user?.username ?: "User"}",
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
                            val startEpoch = remember(home?.programStartDate, user?.createdAt) {
                                val startDate = home?.programStartDate ?: user?.createdAt ?: ""
                                if (startDate.isNotBlank()) {
                                    try {
                                        val parts = startDate.substring(0, 10).split("-")
                                        val startYear = parts[0].toInt()
                                        val startMonth = parts[1].toInt()
                                        val startDay = parts[2].toInt()
                                        dateToEpochDay(startYear, startMonth, startDay)
                                    } catch (e: Exception) {
                                        0L
                                    }
                                } else {
                                    0L
                                }
                            }
                            
                            val startWeekMondayEpoch = remember(startEpoch, startWeekday) {
                                if (startEpoch == 0L) 0L else startEpoch - ((startEpoch + 3) % 7 - startWeekday + 7) % 7
                            }

                            IconButton(
                                onClick = { 
                                    if (trackerStartEpoch - 7 >= startWeekMondayEpoch) {
                                        trackerStartEpoch -= 7 
                                    }
                                },
                                enabled = trackerStartEpoch - 7 >= startWeekMondayEpoch
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft, 
                                    contentDescription = "Previous Week",
                                    tint = if (trackerStartEpoch - 7 >= startWeekMondayEpoch) LocalContentColor.current else Color.Gray
                                )
                            }
                            
                            val startYmd = epochDayToYmd(trackerStartEpoch)
                            val endYmd = epochDayToYmd(trackerStartEpoch + 6)
                            
                            val maxAllowedWeek = home?.currentWeek ?: 1
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Week $displayWeek",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${startYmd.day}.${startYmd.month} - ${endYmd.day}.${endYmd.month}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            val itsNotYetTimeMessage = stringResource(Res.string.its_not_yet_time_week)
                            IconButton(
                                onClick = { 
                                    if (displayWeek < (home?.currentWeek ?: 1)) {
                                        trackerStartEpoch += 7 
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(itsNotYetTimeMessage)
                                        }
                                    }
                                },
                                enabled = true
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight, 
                                    contentDescription = "Next Week",
                                    tint = if (displayWeek < (home?.currentWeek ?: 1)) LocalContentColor.current else Color.Gray
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
                                val currentDayInWeek = i + 1
                                val dayCompletions = allCompletions.filter { it.userId == userId && it.day == currentDayInWeek && it.week == displayWeek && it.completed }
                                val morningCompleted = dayCompletions.any { it.session.equals("MORNING", ignoreCase = true) }
                                val eveningCompleted = dayCompletions.any { it.session.equals("EVENING", ignoreCase = true) }
                                
                                val statusColor = Color.Green
                                
                                val circleBackground = when {
                                    morningCompleted && eveningCompleted -> statusColor
                                    morningCompleted -> Brush.horizontalGradient(
                                        0.0f to statusColor,
                                        0.5f to statusColor,
                                        0.5f to Color.Transparent,
                                        1.0f to Color.Transparent
                                    )
                                    eveningCompleted -> Brush.horizontalGradient(
                                        0.0f to Color.Transparent,
                                        0.5f to Color.Transparent,
                                        0.5f to statusColor,
                                        1.0f to statusColor
                                    )
                                    else -> Color.Transparent
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
                                            .then(
                                                if (isSelected) {
                                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .padding(if (isSelected) 2.dp else 0.dp)
                                            .then(
                                                if (circleBackground is Brush) {
                                                    Modifier.background(circleBackground, CircleShape)
                                                } else if (circleBackground is Color) {
                                                    Modifier.background(circleBackground, CircleShape)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .border(
                                                width = if (isToday) 2.dp else 1.dp,
                                                color = if (isToday) MaterialTheme.colorScheme.secondary else statusColor.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentYmd.day.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (morningCompleted && eveningCompleted) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val morningCompleted = completions.any { it.session == "MORNING" }
                    val eveningCompleted = completions.any { it.session == "EVENING" }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = morningCompleted,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    sessionViewModel.markSessionCompleted(displayWeek, selectedDayInWeek, userId, "MORNING", checked)
                                }
                            }
                        )
                        Text("Morning", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = eveningCompleted,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    sessionViewModel.markSessionCompleted(displayWeek, selectedDayInWeek, userId, "EVENING", checked)
                                }
                            }
                        )
                        Text("Evening", style = MaterialTheme.typography.bodyMedium)
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
                        }
                    }
                }
            }
        }
    }
}
