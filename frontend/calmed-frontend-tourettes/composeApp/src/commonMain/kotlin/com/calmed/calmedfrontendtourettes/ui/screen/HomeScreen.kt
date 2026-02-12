package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.util.currentYmd
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import org.koin.compose.koinInject
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items





@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel = koinInject()
) {

    val home = sessionViewModel.home.collectAsState().value
    val ymd = currentYmd()
    val days = home?.calendar?.days ?: emptyList()



    LaunchedEffect(Unit) {
        sessionViewModel.loadHome(year = ymd.year, month = ymd.month)
    }

    ScreenScaffold(title = "Home") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Welcome to CalmEd Tourettes.")
            Text("Current week: ${home?.currentWeek ?: "-"}")
            Text("Month from backend: ${home?.calendar?.month}")
            Text("Year from backend: ${home?.calendar?.year}")
            LazyVerticalGrid(
                columns = GridCells.Fixed(7)
            ) {
                items(days) { d ->
                    val isToday = d.day == ymd.day
                    Text(
                        text = if (isToday) "[${d.day}]" else "${d.day}"
                    )
                }
            }
            home?.upNext?.forEach { ex ->
                Text("• ${ex.title}")
            }


        }
    }
}
