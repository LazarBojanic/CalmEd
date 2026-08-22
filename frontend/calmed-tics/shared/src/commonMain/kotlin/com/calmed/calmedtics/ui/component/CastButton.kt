package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CastButton(
    hlsUrl: String,
    title: String,
    modifier: Modifier = Modifier
)