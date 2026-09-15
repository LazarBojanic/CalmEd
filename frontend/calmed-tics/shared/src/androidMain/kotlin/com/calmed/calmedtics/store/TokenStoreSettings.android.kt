package com.calmed.calmedtics.store

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.SharedPreferencesSettings

private const val TOKEN_STORE_FILE_NAME = "calmed_token_store"

fun provideTokenStoreSettings(context: Context): TokenStoreSettings =
	TokenStoreSettings(SharedPreferencesSettings(createEncryptedPreferences(context)))

private fun createEncryptedPreferences(context: Context): SharedPreferences {
	val masterKey = MasterKey.Builder(context)
		.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
		.build()

	return EncryptedSharedPreferences.create(
		context,
		TOKEN_STORE_FILE_NAME,
		masterKey,
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)
}
