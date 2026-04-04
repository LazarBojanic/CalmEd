package com.calmed.calmedtics.localization

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var defaultLocale: Locale? = null
    private val LocalAppLocaleState = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    actual val current: String
        @Composable get() = LocalAppLocaleState.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = Configuration(LocalConfiguration.current)

        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault()
        }

        val newLocale = if (value == null) defaultLocale!! else Locale.forLanguageTag(value)
        Locale.setDefault(newLocale)
        configuration.setLocale(newLocale)

        val resources = LocalContext.current.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)

        return LocalAppLocaleState.provides(newLocale.toLanguageTag())
    }
}