package com.calmed.calmedtics.ui.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The iOS player renders its own download button in the top-right corner
 * (12.dp inset, 32.dp button), so the screen's overlay column is pushed
 * below it: 12 + 32 + 8 spacing = 52.dp.
 */
actual val PlayerTopOverlayInset: Dp = 52.dp
