package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining

@Composable
expect fun ThumbnailImage(
    client: HttpClient,
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
)
suspend fun loadImageBytes(client: HttpClient, url: String): ByteArray {
    val channel = client.get(url).bodyAsChannel()
    return channel.readRemaining().readBytes()
}