package com.calmed.calmedfrontendtourettes.ui.screen

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.http.AppHttpClient
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.ui.component.ThumbnailImage
import com.calmed.calmedfrontendtourettes.ui.component.VideoPlayer
import com.calmed.calmedfrontendtourettes.ui.component.NativeCalendar
import com.calmed.calmedfrontendtourettes.util.currentYmd
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import org.koin.compose.koinInject
import kotlin.time.Instant

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel = koinInject(),
    onOpenFullscreen: (String) -> Unit = {}
) {
    val home by sessionViewModel.home.collectAsState(initial = null)
    val user by sessionViewModel.user.collectAsState()
    val allExercises by sessionViewModel.allExercises.collectAsState()
    val ymd = currentYmd()
    val days = home?.calendar?.days ?: emptyList()
    val appHttpClient: AppHttpClient = koinInject()

    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedTitle by remember { mutableStateOf<String?>(null) }
    var selectedWeekNumber by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        sessionViewModel.loadHome(year = ymd.year, month = ymd.month)
    }

    LaunchedEffect(home?.upNext) {
        if (selectedVideoUrl.isNullOrBlank()) {
            val first = home?.upNext?.firstOrNull()
            selectedVideoUrl = first?.videoURL
            selectedTitle = first?.title
        }
    }

    LaunchedEffect(home?.currentWeek) {
        if (selectedWeekNumber == null) {
            selectedWeekNumber = home?.currentWeek ?: 1
        }
    }

    val sortedDays = remember(days) { days.sortedBy { it.day } }
    val activeWeek = selectedWeekNumber ?: home?.currentWeek ?: 1
    val weekExercises = remember(allExercises, activeWeek) {
        allExercises.filter { it.weekNumber == activeWeek }.sortedBy { it.orderInWeek ?: Int.MAX_VALUE }
    }
    val displayExercises = if (weekExercises.isNotEmpty()) weekExercises else home?.upNext.orEmpty()

    LaunchedEffect(displayExercises) {
        if (selectedVideoUrl.isNullOrBlank()) {
            val first = displayExercises.firstOrNull()
            selectedVideoUrl = first?.videoURL
            selectedTitle = first?.title
        }
    }

    ScreenScaffold(title = "Home") {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 72.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Welcome to CalmEd Tourettes.") }

            item {
                if (days.isEmpty()) {
                    Text("Calendar loading...")
                } else {
                    val month = home?.calendar?.month ?: ymd.month
                    val year = home?.calendar?.year ?: ymd.year
                    NativeCalendar(
                        year = year,
                        month = month,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        onDateSelected = { y, m, d ->
                            val createdAt = user?.createdAt
                            if (createdAt.isNullOrBlank()) return@NativeCalendar

                            val programStartEpochDay = try {
                                val parsed = Instant.parse(createdAt)
                                epochDayFromMillis(parsed.toEpochMilliseconds())
                            } catch (_: Throwable) {
                                return@NativeCalendar
                            }

                            val selectedEpochDay = dateToEpochDay(y, m, d)
                            val diffDays = selectedEpochDay - programStartEpochDay
                            val weekIndex = if (diffDays < 0) 0 else diffDays / 7
                            selectedWeekNumber = (weekIndex + 1).toInt()
                        }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!selectedTitle.isNullOrBlank()) {
                        Text(selectedTitle!!)
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
                                Text("Select a video")
                            }
                        }
                    }
                }
            }

            listItems(displayExercises) { ex ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedVideoUrl = ex.videoURL
                            selectedTitle = ex.title
                        }
                ) {
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
                                    client = appHttpClient.client,
                                    url = thumb,
                                    contentDescription = ex.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No image")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))


                        Text(
                            text = ex.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
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
