package com.calmed.calmedfrontendtourettes.settings

import com.russhwolf.settings.Settings

class AppSettings(
    private val settings: Settings
) {
    companion object {
        private const val KEY_SHOW_WELCOME_VIDEO = "showWelcomeVideo"
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

}