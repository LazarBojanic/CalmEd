package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient

@Composable
expect fun ThumbnailImage(
    client: HttpClient,
    url: String,
    contentDescription: String?,
    modifier: Modifier
)
