
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
fun inter18pt(): FontFamily {
    return FontFamily(
        Font(Res.font.Inter_18pt_Thin, FontWeight.Thin, FontStyle.Normal),
        Font(Res.font.Inter_18pt_ThinItalic, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.Inter_18pt_ExtraLight, FontWeight.ExtraLight, FontStyle.Normal),
        Font(Res.font.Inter_18pt_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.Inter_18pt_Light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.Inter_18pt_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.Inter_18pt_Regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.Inter_18pt_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.Inter_18pt_Medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.Inter_18pt_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.Inter_18pt_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.Inter_18pt_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.Inter_18pt_Bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.Inter_18pt_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.Inter_18pt_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(Res.font.Inter_18pt_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(Res.font.Inter_18pt_Black, FontWeight.Black, FontStyle.Normal),
        Font(Res.font.Inter_18pt_BlackItalic, FontWeight.Black, FontStyle.Italic),
    )
}

@Composable
fun inter24pt(): FontFamily {
    return FontFamily(
        Font(Res.font.Inter_24pt_Thin, FontWeight.Thin, FontStyle.Normal),
        Font(Res.font.Inter_24pt_ThinItalic, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.Inter_24pt_ExtraLight, FontWeight.ExtraLight, FontStyle.Normal),
        Font(Res.font.Inter_24pt_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.Inter_24pt_Light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.Inter_24pt_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.Inter_24pt_Regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.Inter_24pt_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.Inter_24pt_Medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.Inter_24pt_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.Inter_24pt_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.Inter_24pt_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.Inter_24pt_Bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.Inter_24pt_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.Inter_24pt_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(Res.font.Inter_24pt_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(Res.font.Inter_24pt_Black, FontWeight.Black, FontStyle.Normal),
        Font(Res.font.Inter_24pt_BlackItalic, FontWeight.Black, FontStyle.Italic),
    )
}

@Composable
fun inter28pt(): FontFamily {
    return FontFamily(
        Font(Res.font.Inter_28pt_Thin, FontWeight.Thin, FontStyle.Normal),
        Font(Res.font.Inter_28pt_ThinItalic, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.Inter_28pt_ExtraLight, FontWeight.ExtraLight, FontStyle.Normal),
        Font(Res.font.Inter_28pt_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.Inter_28pt_Light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.Inter_28pt_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.Inter_28pt_Regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.Inter_28pt_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.Inter_28pt_Medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.Inter_28pt_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.Inter_28pt_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.Inter_28pt_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.Inter_28pt_Bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.Inter_28pt_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.Inter_28pt_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(Res.font.Inter_28pt_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(Res.font.Inter_28pt_Black, FontWeight.Black, FontStyle.Normal),
        Font(Res.font.Inter_28pt_BlackItalic, FontWeight.Black, FontStyle.Italic),
    )
}

@Composable
fun interFont(): Typography {
    val bodyFont = inter18pt()
    val titleFont = inter24pt()
    val displayFont = inter28pt()

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(
                fontFamily = displayFont,
                fontWeight = FontWeight.ExtraBold
            ),
            displayMedium = displayMedium.copy(
                fontFamily = displayFont,
                fontWeight = FontWeight.Bold
            ),
            displaySmall = displaySmall.copy(
                fontFamily = displayFont,
                fontWeight = FontWeight.Bold
            ),

            headlineLarge = headlineLarge.copy(
                fontFamily = displayFont,
                fontWeight = FontWeight.Bold
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = titleFont,
                fontWeight = FontWeight.SemiBold
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = titleFont,
                fontWeight = FontWeight.SemiBold
            ),

            titleLarge = titleLarge.copy(
                fontFamily = titleFont,
                fontWeight = FontWeight.SemiBold
            ),
            titleMedium = titleMedium.copy(
                fontFamily = titleFont,
                fontWeight = FontWeight.SemiBold
            ),
            titleSmall = titleSmall.copy(
                fontFamily = titleFont,
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
            ),
        )
    }
}
