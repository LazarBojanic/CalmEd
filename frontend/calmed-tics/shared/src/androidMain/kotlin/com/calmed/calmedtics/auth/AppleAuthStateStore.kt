package com.calmed.calmedtics.auth

import android.content.Context

object AppleAuthStateStore {
	private const val PREFS_NAME = "apple_auth"
	private const val KEY_EXPECTED_STATE = "expected_state"

	fun save(context: Context, state: String) {
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.edit()
			.putString(KEY_EXPECTED_STATE, state)
			.apply()
	}

	fun consume(context: Context): String? {
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		val state = prefs.getString(KEY_EXPECTED_STATE, null)
		prefs.edit().remove(KEY_EXPECTED_STATE).apply()
		return state
	}
}
