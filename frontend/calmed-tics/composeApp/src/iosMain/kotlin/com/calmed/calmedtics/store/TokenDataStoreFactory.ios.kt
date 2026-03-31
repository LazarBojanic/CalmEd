package com.calmed.calmedtics.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun provideTokenDataStore(): DataStore<Preferences> =
	createTokenDataStore {
		val dir = NSFileManager.defaultManager.URLForDirectory(
			directory = NSDocumentDirectory,
			inDomain = NSUserDomainMask,
			appropriateForURL = null,
			create = false,
			error = null
		)
		requireNotNull(dir).path + "/$TOKEN_DATASTORE_FILE_NAME"
	}