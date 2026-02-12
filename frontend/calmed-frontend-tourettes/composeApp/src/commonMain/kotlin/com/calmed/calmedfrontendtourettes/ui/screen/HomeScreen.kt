package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedTitle by remember { mutableStateOf<String?>(null) }

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

    ScreenScaffold(title = "Home") {
        LazyColumn(
        modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .padding(bottom = 72.dp) ,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Welcome to CalmEd Tourettes.") }

            item {
                if (days.isEmpty()) {
                    Text("Calendar loading...")
                } else {
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
                                    modifier = Modifier.fillMaxSize()
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

            listItems(home?.upNext ?: emptyList()) { ex ->
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
