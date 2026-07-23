package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.view.ContextThemeWrapper
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.calmed.calmedtics.R

@Composable
actual fun CastButton(
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f)),
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