package com.calmed.calmedtics.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences


fun provideTokenDataStore(context: Context): DataStore<Preferences> =
	createTokenDataStore {
		context.filesDir.resolve(TOKEN_DATASTORE_FILE_NAME).absolutePath
	}