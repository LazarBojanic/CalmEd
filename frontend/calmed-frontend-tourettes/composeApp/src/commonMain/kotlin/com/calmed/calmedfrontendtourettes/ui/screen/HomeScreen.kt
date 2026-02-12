package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.http.AppHttpClient
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.ui.component.ThumbnailImage
import com.calmed.calmedfrontendtourettes.ui.component.VideoPlayer
import com.calmed.calmedfrontendtourettes.util.currentYmd
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import org.koin.compose.koinInject


@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel = koinInject()
) {
    val home by sessionViewModel.home.collectAsState(initial = null)
    val ymd = currentYmd()
    val days = home?.calendar?.days ?: emptyList()
    val appHttpClient: AppHttpClient = koinInject()


    var expandedId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        sessionViewModel.loadHome(year = ymd.year, month = ymd.month)
    }


    ScreenScaffold(title = "Home") {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Welcome to CalmEd Tourettes.") }
            item { Text("Current week: ${home?.currentWeek ?: "-"}") }


            item { Text("Month from backend: ${home?.calendar?.month}") }
            item { Text("Year from backend: ${home?.calendar?.year}") }


            item {
                val rows = (days.size + 6) / 7
                val gridHeight = (rows * 40).dp

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight)
                ) {
                    gridItems(days) { d ->
                        val isToday = d.day == ymd.day
                        Text(if (isToday) "[${d.day}]" else "${d.day}")
                    }
                }
            }


            listItems(home?.upNext ?: emptyList()) { ex ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(ex.title)

                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clickable {
                                expandedId = if (expandedId == ex.id) null else ex.id
                            }
                    ) {
                        val showPlayer = (expandedId == ex.id)
                        val videoUrl = ex.videoURL
                        val thumb = ex.thumbnailURL

                        if (showPlayer && !videoUrl.isNullOrBlank()) {

                            VideoPlayer(
                                hlsUrl = videoUrl,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {

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
                                    Text("Tap to play")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
