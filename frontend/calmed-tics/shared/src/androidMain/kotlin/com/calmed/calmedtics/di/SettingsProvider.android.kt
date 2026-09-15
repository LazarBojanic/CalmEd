package com.calmed.calmedtics.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

fun provideSettings(context: Context): Settings =
    SharedPreferencesSettings(
        context.getSharedPreferences("calmed_settings", Context.MODE_PRIVATE)
    )
