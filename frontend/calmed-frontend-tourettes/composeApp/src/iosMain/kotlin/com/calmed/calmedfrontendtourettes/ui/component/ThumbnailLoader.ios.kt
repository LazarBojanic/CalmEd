package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient

@Composable
actual fun ThumbnailImage(
    client: HttpClient,
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
}