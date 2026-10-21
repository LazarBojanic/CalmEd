
package com.calmed.calmedtics.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val lightScheme = lightColorScheme(
    // Primary — brand purple
    primary = purple007,
    onPrimary = white,
    primaryContainer = purple002,
    onPrimaryContainer = purple010,

    // Secondary — brand pink
    secondary = pink005,
    onSecondary = black,
    secondaryContainer = pink002,
    onSecondaryContainer = pink010,

    // Tertiary — blue accent
    tertiary = blue008,
    onTertiary = white,
    tertiaryContainer = blue002,
    onTertiaryContainer = blue010,

    // Error — red
    error = red008,
    onError = white,
    errorContainer = red002,
    onErrorContainer = red010,

    // Background and surfaces
    background = gray001,
    onBackground = gray012,
    surface = white,
    onSurface = gray012,
    surfaceVariant = gray002,
    onSurfaceVariant = gray008,

    // Borders
    outline = gray007,
    outlineVariant = gray004,

    // Overlays and inverse colors
    scrim = black,
    inverseSurface = gray011,
    inverseOnSurface = gray001,
    inversePrimary = purple004,

    // Surface elevation levels
    surfaceDim = gray003,
    surfaceBright = white,
    surfaceContainerLowest = white,
    surfaceContainerLow = gray001,
    surfaceContainer = gray002,
    surfaceContainerHigh = gray003,
    surfaceContainerHighest = gray003,
)

val darkScheme = darkColorScheme(
    // Primary — lighter purple for dark backgrounds
    primary = purple004,
    onPrimary = purple012,
    primaryContainer = purple010,
    onPrimaryContainer = purple002,

    // Secondary — lighter pink
    secondary = pink005,
    onSecondary = pink012,
    secondaryContainer = pink010,
    onSecondaryContainer = pink002,

    // Tertiary — lighter blue
    tertiary = blue005,
    onTertiary = blue012,
    tertiaryContainer = blue010,
    onTertiaryContainer = blue002,

    // Error — lighter red
    error = red005,
    onError = red012,
    errorContainer = red010,
    onErrorContainer = red002,

    // Background and surfaces
    background = gray012,
    onBackground = gray001,
    surface = gray012,
    onSurface = gray001,
    surfaceVariant = gray010,
    onSurfaceVariant = gray004,

    // Borders
    outline = gray006,
    outlineVariant = gray009,

    // Overlays and inverse colors
    scrim = black,
    inverseSurface = gray001,
    inverseOnSurface = gray011,
    inversePrimary = purple007,

    // Surface elevation levels
    surfaceDim = black,
    surfaceBright = gray008,
    surfaceContainerLowest = black,
    surfaceContainerLow = gray012,
    surfaceContainer = gray011,
    surfaceContainerHigh = gray010,
    surfaceContainerHighest = gray009,
)