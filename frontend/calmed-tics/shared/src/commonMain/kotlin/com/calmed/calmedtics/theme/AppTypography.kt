package com.calmed.calmedtics.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.calmed.calmedtics.font.inter18pt
import com.calmed.calmedtics.font.plusJakartaSans

@Composable
fun appTypography(): Typography {
    val headingFont = plusJakartaSans()
    val bodyFont = inter18pt()

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold
            ),
            displayMedium = displayMedium.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.Bold
            ),
            displaySmall = displaySmall.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.Bold
            ),

            headlineLarge = headlineLarge.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.Bold
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.SemiBold
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.SemiBold
            ),

            titleLarge = titleLarge.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.SemiBold
            ),
            titleMedium = titleMedium.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.SemiBold
            ),
            titleSmall = titleSmall.copy(
                fontFamily = headingFont,
                fontWeight = FontWeight.Medium
            ),

            labelLarge = labelLarge.copy(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Medium
            ),
            labelMedium = labelMedium.copy(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Medium
            ),
            labelSmall = labelSmall.copy(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Medium
            ),

            bodyLarge = bodyLarge.copy(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Normal
            ),
            bodyMedium = bodyMedium.copy(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Normal
            ),
            bodySmall = bodySmall.copy(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Normal
            )
        )
    }
}