package com.calmed.calmedtics.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import com.calmed.calmedtics.font.interFont
import com.calmed.calmedtics.font.plusJakartaSansFont

enum class AppFont {
    PlusJakartaSans,
    Inter
}

@Composable
fun appTypography(
    font: AppFont = AppFont.PlusJakartaSans
): Typography {
    return when (font) {
        AppFont.PlusJakartaSans -> plusJakartaSansFont()
        AppFont.Inter -> interFont()
    }
}