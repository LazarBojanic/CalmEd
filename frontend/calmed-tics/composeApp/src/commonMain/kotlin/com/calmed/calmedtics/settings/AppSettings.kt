package com.calmed.calmedtics.settings

import com.russhwolf.settings.Settings

class AppSettings(
    private val settings: Settings
) {
    companion object {
        private const val KEY_SHOW_WELCOME_VIDEO = "showWelcomeVideo"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_APP_LANGUAGE = "app_language"
    }

    private fun welcomeVideoKey(userId: String?): String {
        val suffix = userId?.takeIf { it.isNotBlank() }
        return if (suffix == null) KEY_SHOW_WELCOME_VIDEO else "$KEY_SHOW_WELCOME_VIDEO:$suffix"
    }

    fun getShowWelcomeVideo(userId: String?): Boolean =
        settings.getBoolean(welcomeVideoKey(userId), defaultValue = true)


    fun setShowWelcomeVideo(userId: String?, value: Boolean) {
        settings.putBoolean(welcomeVideoKey(userId), value)
    }
    fun isRemindersEnabled(): Boolean =
        settings.getBoolean(KEY_REMINDERS_ENABLED, false)

    fun setRemindersEnabled(value: Boolean) {
        settings.putBoolean(KEY_REMINDERS_ENABLED, value)
    }
    fun getAppLanguage(): String? =
        settings.getStringOrNull(KEY_APP_LANGUAGE)

    fun setAppLanguage(value: String?) {
        if (value == null) {
            settings.remove(KEY_APP_LANGUAGE)
        } else {
            settings.putString(KEY_APP_LANGUAGE, value)
        }
    }
}