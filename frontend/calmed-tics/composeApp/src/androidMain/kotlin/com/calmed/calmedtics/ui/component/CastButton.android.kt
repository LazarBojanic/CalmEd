package com.calmed.calmedtics.ui.component

import android.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.calmed.calmedtics.R
import com.google.android.gms.cast.framework.CastButtonFactory

@Composable
actual fun CastButton(
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MediaRouteButton(
                    ContextThemeWrapper(
                        context,
                        R.style.CalmEdCastButtonTheme
                    )
                ).apply {
                    CastButtonFactory.setUpMediaRouteButton(context, this)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            }
        )
    }
}