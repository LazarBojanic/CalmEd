package com.calmed.calmedtics.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CastButton(
    hlsUrl: String,
    title: String,
    modifier: Modifier
) {
    // On iOS, AVPlayerViewController / AVRoutePickerView natively handles AirPlay and casting.
    Box(modifier = modifier)
}
