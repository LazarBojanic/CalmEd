package com.calmed.calmedtics.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

actual fun provideSettings(): Settings =
    NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)