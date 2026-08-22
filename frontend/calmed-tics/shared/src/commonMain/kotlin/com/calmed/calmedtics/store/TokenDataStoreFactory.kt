package com.calmed.calmedtics.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val TOKEN_DATASTORE_FILE_NAME = "token_datastore.preferences_pb"

fun createTokenDataStore(producePath: () -> String): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath( produceFile = { producePath().toPath() }
)