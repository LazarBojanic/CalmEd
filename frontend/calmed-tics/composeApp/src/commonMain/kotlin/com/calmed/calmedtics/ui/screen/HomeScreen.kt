package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.calmed.calmedtics.default_user
import com.calmed.calmedtics.hello_user
import com.calmed.calmedtics.home_welcome
import com.calmed.calmedtics.previous_week
import com.calmed.calmedtics.week_number
import com.calmed.calmedtics.no_image
import com.calmed.calmedtics.select_video
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.ThumbnailImage
import com.calmed.calmedtics.util.currentYmd
import com.calmed.calmedtics.util.dateToEpochDay
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.calmed.calmedtics.its_not_yet_time_week
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.unit.sp
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.util.epochDayToYmd
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel = koinInject(),
    onExerciseClick: (ProgramExerciseDto) -> Unit,
    onOpenVideo: (String) -> Unit = {}
) {
    val home by sessionViewModel.home.collectAsState(initial = null)
    val user by sessionViewModel.user.collectAsState()
    val userInfo by sessionViewModel.userInfo.collectAsState()
    val allExercises by sessionViewModel.allExercises.collectAsState()
    val allCompletions by sessionViewModel.allCompletions.collectAsState()


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
    val selectedTrackerYmd =
        remember(selectedTrackerDateEpoch) { epochDayToYmd(selectedTrackerDateEpoch) }
    val selectedDateStr = "${selectedTrackerYmd.year}-${
        selectedTrackerYmd.month.toString().padStart(2, '0')
    }-${selectedTrackerYmd.day.toString().padStart(2, '0')}"

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
        allExercises.filter { it.weekNumber == displayWeek }
            .sortedBy { it.orderInWeek ?: Int.MAX_VALUE }
    }
    val displayExercises = weekExercises.ifEmpty { home?.upNext.orEmpty() }
    val selectedExercise = remember(displayExercises, selectedExerciseId) {
        displayExercises.find { it.id == selectedExerciseId } ?: displayExercises.firstOrNull()
    }
    val selectedVideoUrl = remember(
        selectedExercise
    ) { selectedExercise?.videoURL }
    val selectedTitle =
        remember(selectedExercise) { selectedExercise?.title }

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

            /*
             * HEADER
             */
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            Res.string.hello_user,
                            if (userInfo?.preferredName.isNullOrBlank()) {
                                user?.username ?: stringResource(Res.string.default_user)
                            } else {
                                userInfo?.preferredName!!
                            }
                        ),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(Res.string.home_welcome),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            /*
 * CALENDAR
 */
            item {
                val startEpoch = remember(
                    home?.programStartDate,
                    user?.createdAt
                ) {
                    val startDate =
                        home?.programStartDate ?: user?.createdAt ?: ""

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

                val startWeekMondayEpoch =
                    remember(startEpoch, startWeekday) {
                        if (startEpoch == 0L) {
                            0L
                        } else {
                            startEpoch -
                                    ((startEpoch + 3) % 7 - startWeekday + 7) % 7
                        }
                    }

                val startYmd = epochDayToYmd(trackerStartEpoch)
                val endYmd = epochDayToYmd(trackerStartEpoch + 6)

                val itsNotYetTimeMessage =
                    stringResource(Res.string.its_not_yet_time_week)

                Card(
                    modifier = Modifier.fillMaxWidth(),
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

                        /*
                         * GORNJI DEO KALENDARA
                         */
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
                                    text =
                                        "${startYmd.day}.${startYmd.month}. – " +
                                                "${endYmd.day}.${endYmd.month}.",
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
                                            enabled =
                                                trackerStartEpoch - 7 >=
                                                        startWeekMondayEpoch
                                        ) {
                                            trackerStartEpoch -= 7
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription =
                                            stringResource(Res.string.previous_week),
                                        tint =
                                            if (
                                                trackerStartEpoch - 7 >=
                                                startWeekMondayEpoch
                                            ) {
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
                                            if (
                                                displayWeek <
                                                (home?.currentWeek ?: 1)
                                            ) {
                                                trackerStartEpoch += 7
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        itsNotYetTimeMessage
                                                    )
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next week",
                                        tint =
                                            if (
                                                displayWeek <
                                                (home?.currentWeek ?: 1)
                                            ) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            },
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                            }
                        }

                        /*
                         * DANI
                         */
                        val daysOfWeekFull =
                            listOf("M", "T", "W", "T", "F", "S", "S")

                        val daysOfWeek =
                            List(7) { index ->
                                daysOfWeekFull[
                                    (startWeekday + index) % 7
                                ]
                            }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (index in 0..6) {
                                val currentEpoch =
                                    trackerStartEpoch + index

                                val currentYmd =
                                    epochDayToYmd(currentEpoch)

                                val isSelected =
                                    currentEpoch == selectedTrackerDateEpoch

                                val isToday =
                                    currentEpoch == todayEpoch

                                val currentDayInWeek =
                                    index + 1

                                val dayCompletions =
                                    allCompletions.filter {
                                        it.userId == userId &&
                                                it.day == currentDayInWeek &&
                                                it.week == displayWeek &&
                                                it.completed
                                    }

                                val morningCompleted =
                                    dayCompletions.any {
                                        it.session.equals(
                                            "MORNING",
                                            ignoreCase = true
                                        )
                                    }

                                val eveningCompleted =
                                    dayCompletions.any {
                                        it.session.equals(
                                            "EVENING",
                                            ignoreCase = true
                                        )
                                    }

                                Column(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .clickable {
                                            selectedTrackerDateEpoch =
                                                currentEpoch
                                        },
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally,
                                    verticalArrangement =
                                        Arrangement.spacedBy(7.dp)
                                ) {
                                    Text(
                                        text = daysOfWeek[index],
                                        fontSize = 11.sp,
                                        fontWeight =
                                            if (isSelected) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Medium
                                            },
                                        color =
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .then(
                                                when {
                                                    isSelected -> {
                                                        Modifier.background(
                                                            brush =
                                                                Brush.linearGradient(
                                                                    colors = listOf(
                                                                        MaterialTheme.colorScheme.primary,
                                                                        MaterialTheme.colorScheme.secondary
                                                                    )
                                                                ),
                                                            shape = CircleShape
                                                        )
                                                    }

                                                    isToday -> {
                                                        Modifier
                                                            .background(
                                                                color = MaterialTheme.colorScheme.surface,
                                                                shape = CircleShape
                                                            )
                                                            .border(
                                                                width = 1.5.dp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                shape = CircleShape
                                                            )
                                                    }

                                                    else -> {
                                                        Modifier.background(
                                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            shape = CircleShape
                                                        )
                                                    }
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentYmd.day.toString(),
                                            fontSize = 14.sp,
                                            fontWeight =
                                                if (isSelected || isToday) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.SemiBold
                                                },
                                            color =
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                        )
                                    }

                                    /*
                                     * LEVA TAČKA = MORNING
                                     * DESNA TAČKA = EVENING
                                     */
                                    Row(
                                        modifier = Modifier.height(7.dp),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(5.dp),
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(
                                                    color =
                                                        if (morningCompleted) {
                                                            MaterialTheme.colorScheme.tertiary
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
                                                    color =
                                                        if (eveningCompleted) {
                                                            MaterialTheme.colorScheme.primary
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

            /*
 * TODAY'S EXERCISE
 */
            item {
                val thumbnailUrl = selectedExercise?.thumbnailURL

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clickable(enabled = selectedExercise != null) {
                            selectedExercise?.let { exercise ->
                                onExerciseClick(exercise)
                            }
                        },
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
                            ThumbnailImage(
                                url = thumbnailUrl,
                                contentDescription = selectedTitle ?: "",
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
                                        brush = Brush.horizontalGradient(
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
                                    text = "TODAY'S EXERCISE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Text(
                                text = selectedTitle.orEmpty(),
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
                                    text = selectedExercise?.durationSeconds?.let { totalSeconds ->
                                        val minutes = totalSeconds / 60
                                        val seconds = totalSeconds % 60

                                        "${minutes}:${seconds.toString().padStart(2, '0')} min"
                                    } ?: "--:-- min",
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
                                    text = "Week $displayWeek",
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
                                contentDescription =
                                    stringResource(Res.string.select_video),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(start = 2.dp)
                            )
                        }
                    }
                }
            }

            /*
        * MORNING / EVENING
        */
            item {
                val morningCompleted =
                    completions.any {
                        it.session.equals(
                            "MORNING",
                            ignoreCase = true
                        ) && it.completed
                    }

                val eveningCompleted =
                    completions.any {
                        it.session.equals(
                            "EVENING",
                            ignoreCase = true
                        ) && it.completed
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    /*
                     * MORNING
                     */
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                scope.launch {
                                    sessionViewModel.markSessionCompleted(
                                        displayWeek,
                                        selectedDayInWeek,
                                        userId,
                                        "MORNING",
                                        !morningCompleted
                                    )
                                }
                            },
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
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "☀",
                                        fontSize = 23.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "Morning",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Text(
                                            text =
                                                if (morningCompleted) {
                                                    "Completed"
                                                } else {
                                                    "Pending"
                                                },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color =
                                                if (morningCompleted) {
                                                    MaterialTheme.colorScheme.tertiary
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
                                                            color = MaterialTheme.colorScheme.tertiary,
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
                                                Text(
                                                    text = "✓",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onTertiary
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
                                text =
                                    if (morningCompleted) {
                                        "Great start!"
                                    } else {
                                        "Start your day"
                                    },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    /*
                     * EVENING
                     */
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                scope.launch {
                                    sessionViewModel.markSessionCompleted(
                                        displayWeek,
                                        selectedDayInWeek,
                                        userId,
                                        "EVENING",
                                        !eveningCompleted
                                    )
                                }
                            },
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
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "☾",
                                        fontSize = 23.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "Evening",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Text(
                                            text =
                                                if (eveningCompleted) {
                                                    "Completed"
                                                } else {
                                                    "Pending"
                                                },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color =
                                                if (eveningCompleted) {
                                                    MaterialTheme.colorScheme.primary
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
                                                            color = MaterialTheme.colorScheme.primary,
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
                                                Text(
                                                    text = "✓",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
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
                                text =
                                    if (eveningCompleted) {
                                        "Well done!"
                                    } else {
                                        "Keep going!"
                                    },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Exercises this week",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            listItemsIndexed(displayExercises) { index, exercise ->

                val displayTitle =
                    exercise.title

                val isSelected =
                    exercise.id == selectedExercise?.id

                val exerciseNumber =
                    exercise.orderInWeek ?: (index + 1)

                val formattedDuration =
                    exercise.durationSeconds?.let { totalSeconds ->
                        val minutes = totalSeconds / 60
                        val seconds = totalSeconds % 60

                        "${minutes}:${seconds.toString().padStart(2, '0')} min"
                    } ?: "--:-- min"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .clickable {
                            selectedExerciseId = exercise.id
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation =
                            if (isSelected) 3.dp else 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 10.dp,
                                top = 10.dp,
                                end = 12.dp,
                                bottom = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Card(
                            modifier = Modifier.size(
                                width = 108.dp,
                                height = 90.dp
                            ),
                            shape = RoundedCornerShape(15.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            )
                        ) {
                            val thumbnail =
                                exercise.thumbnailURL

                            if (!thumbnail.isNullOrBlank()) {
                                ThumbnailImage(
                                    url = thumbnail,
                                    contentDescription = displayTitle,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.no_image
                                        ),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = displayTitle,
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "Week ${exercise.weekNumber} · Exercise $exerciseNumber",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(
                                modifier = Modifier.height(7.dp)
                            )

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(7.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(19.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(
                                                    color = MaterialTheme.colorScheme.primary,
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
                                    contentAlignment =
                                        Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }

                                Text(
                                    text = formattedDuration,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Icon(
                            imageVector =
                                Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }
        }
    }
}