package com.calmed.calmedtics.ui.component
 
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
 
@Composable
fun ThumbnailImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    if (url.isBlank()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
        }
        return
    }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
