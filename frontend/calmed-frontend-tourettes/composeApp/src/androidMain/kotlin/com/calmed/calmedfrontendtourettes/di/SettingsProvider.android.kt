package com.calmed.calmedfrontendtourettes.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

lateinit var appContext: Context

actual fun provideSettings(): Settings =
    SharedPreferencesSettings(
        appContext.getSharedPreferences("calmed_settings", Context.MODE_PRIVATE)
    )