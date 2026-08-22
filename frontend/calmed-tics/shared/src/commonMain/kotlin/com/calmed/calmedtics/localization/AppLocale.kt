package com.calmed.calmedtics.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

var customAppLocale: String? = "en"

expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

@Composable
fun AppLocaleProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppLocale provides "en"
    ) {
        content()
    }
}