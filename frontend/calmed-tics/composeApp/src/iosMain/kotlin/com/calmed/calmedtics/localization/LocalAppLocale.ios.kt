package com.calmed.calmedtics.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    private val defaultLocale = (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"
    private val LocalAppLocaleState = staticCompositionLocalOf { defaultLocale }

    actual val current: String
        @Composable get() = LocalAppLocaleState.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val newValue = value ?: defaultLocale

        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        } else {
            NSUserDefaults.standardUserDefaults.setObject(listOf(newValue), LANG_KEY)
        }

        return LocalAppLocaleState.provides(newValue)
    }
}