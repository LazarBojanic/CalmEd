
package com.calmed.calmedtics.font

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import calmedtics.shared.generated.resources.*
import calmedtics.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun plusJakartaSans(): FontFamily {
    return FontFamily(
        Font(Res.font.PlusJakartaSans_Bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.PlusJakartaSans_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.PlusJakartaSans_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(Res.font.PlusJakartaSans_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(Res.font.PlusJakartaSans_ExtraLight, FontWeight.ExtraLight, FontStyle.Normal),
        Font(Res.font.PlusJakartaSans_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.PlusJakartaSans_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.PlusJakartaSans_Light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.PlusJakartaSans_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.PlusJakartaSans_Medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.PlusJakartaSans_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.PlusJakartaSans_Regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.PlusJakartaSans_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.PlusJakartaSans_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
    )
}

@Composable
fun plusJakartaSansFont(): Typography {
    val fontFamily = plusJakartaSans()

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.ExtraBold
            ),
            displayMedium = displayMedium.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            ),
            displaySmall = displaySmall.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            ),

            headlineLarge = headlineLarge.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold
            ),

            titleLarge = titleLarge.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            titleMedium = titleMedium.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            titleSmall = titleSmall.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium
            ),

            labelLarge = labelLarge.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium
            ),
            labelMedium = labelMedium.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium
            ),
            labelSmall = labelSmall.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium
            ),

            bodyLarge = bodyLarge.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal
            ),
            bodyMedium = bodyMedium.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal
            ),
            bodySmall = bodySmall.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal
            )
        )
    }
}
