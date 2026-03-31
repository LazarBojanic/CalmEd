package com.calmed.calmedtics.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeCalendar(
    year: Int,
    month: Int,
    modifier: Modifier = Modifier,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit = { _, _, _ -> }
)
