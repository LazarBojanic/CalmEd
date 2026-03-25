package com.calmed.calmedfrontendtourettes.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.CancellationException
import org.jetbrains.skia.Image

@Composable
actual fun ThumbnailImage(
    client: HttpClient,
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    val bmpState = produceState<ImageBitmap?>(initialValue = null, key1 = url) {
        value = null
        if (url.isBlank()) return@produceState
        try {
            val bytes: ByteArray = client.get(url) {
                // Remove Authorization header and JSON content types for thumbnail requests to external CDNs
                header("Authorization", null)
                header("Accept", "image/*")
                header("Content-Type", null)
            }.body()
            val skiaImage = org.jetbrains.skia.Image.makeFromEncoded(bytes)
            value = skiaImage?.toComposeImageBitmap()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Throwable) {
            println("ThumbnailImage error for $url: ${e.toString()}")
            value = null
        }
    }.value

    if (bmpState != null) {
        Image(
            bitmap = bmpState,
            contentDescription = contentDescription,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Placeholder can be added here
        }
    }
}