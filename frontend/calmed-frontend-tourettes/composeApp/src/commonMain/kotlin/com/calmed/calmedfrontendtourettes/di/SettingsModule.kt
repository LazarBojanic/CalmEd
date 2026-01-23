package com.calmed.calmedfrontendtourettes.di

import com.calmed.calmedfrontendtourettes.settings.AppSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

expect fun provideSettings(): Settings

val settingsModule = module {
    single { provideSettings() }
    single { AppSettings(get()) }
}