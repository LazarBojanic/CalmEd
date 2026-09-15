package com.calmed.calmedtics.store

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings

@OptIn(ExperimentalSettingsImplementation::class)
fun provideTokenStoreSettings(): TokenStoreSettings =
	TokenStoreSettings(KeychainSettings(service = "com.calmed.calmedtics.tokens"))
