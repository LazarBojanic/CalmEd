package com.calmed.calmedfrontendtourettes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmed.calmedfrontendtourettes.ui.component.ScreenScaffold
import com.calmed.calmedfrontendtourettes.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun HomeScreen(

) {
    val scope = rememberCoroutineScope()
    ScreenScaffold(title = "Home"){
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Welcome to CalmEd Tourettes.")

        }
    }

}