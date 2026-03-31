package com.calmed.calmedtics.di

import com.calmed.calmedtics.settings.AppSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

expect fun provideSettings(): Settings

val settingsModule = module {
    single { provideSettings() }
    single { AppSettings(get()) }
}