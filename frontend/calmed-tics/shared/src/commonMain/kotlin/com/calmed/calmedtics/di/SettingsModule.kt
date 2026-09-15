package com.calmed.calmedtics.di

import com.calmed.calmedtics.settings.AppSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

val appSettingsModule = module {
    single { AppSettings(get()) }
}
