
package com.calmed.calmedtics.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val lightScheme = lightColorScheme(
    primary = purple,
    onPrimary = white,
    primaryContainer = purple002,
    onPrimaryContainer = purple010,

    secondary = pink,
    onSecondary = black,
    secondaryContainer = pink002,
    onSecondaryContainer = pink010,

    tertiary = blue003,
    onTertiary = blue011,
    tertiaryContainer = blue005,
    onTertiaryContainer = blue011,

    error = red008,
    onError = white,
    errorContainer = red002,
    onErrorContainer = red010,

    background = gray001,
    onBackground = gray012,
    surface = white,
    onSurface = gray012,
    surfaceVariant = gray002,
    onSurfaceVariant = gray008,

    outline = gray007,
    outlineVariant = gray004,

    scrim = black,
    inverseSurface = gray011,
    inverseOnSurface = gray001,
    inversePrimary = purple004,

    surfaceDim = gray003,
    surfaceBright = white,
    surfaceContainerLowest = white,
    surfaceContainerLow = gray001,
    surfaceContainer = gray002,
    surfaceContainerHigh = gray003,
    surfaceContainerHighest = gray003,
)

val darkScheme = darkColorScheme(
    primary = purple008,
    onPrimary = purple012,
    primaryContainer = purple010,
    onPrimaryContainer = purple002,

    secondary = pink007,
    onSecondary = pink012,
    secondaryContainer = pink010,
    onSecondaryContainer = pink002,

    tertiary = blue008,
    onTertiary = blue002,
    tertiaryContainer = blue011,
    onTertiaryContainer = blue002,

    error = red004,
    onError = red012,
    errorContainer = red010,
    onErrorContainer = red002,

    background = gray012,
    onBackground = gray001,
    surface = gray012,
    onSurface = gray001,
    surfaceVariant = gray010,
    onSurfaceVariant = gray004,

    outline = gray006,
    outlineVariant = gray009,

    scrim = black,
    inverseSurface = gray001,
    inverseOnSurface = gray011,
    inversePrimary = purple007,

    surfaceDim = black,
    surfaceBright = gray008,
    surfaceContainerLowest = black,
    surfaceContainerLow = gray012,
    surfaceContainer = gray011,
    surfaceContainerHigh = gray010,
    surfaceContainerHighest = gray009,
)