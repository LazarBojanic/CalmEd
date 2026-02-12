package com.calmed.calmedfrontendtourettes.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.HttpClient

@Composable
actual fun ThumbnailImage(
    client: HttpClient,
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    val bmpState = remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        runCatching {
            val bytes = loadImageBytes(client, url) // <-- dolazi iz commonMain
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bmpState.value = bmp?.asImageBitmap()
        }
    }

    bmpState.value?.let { bmp ->
        Image(
            bitmap = bmp,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
