package com.calmed.calmedfrontendtourettes.settings

import com.russhwolf.settings.Settings

class AppSettings(
    private val settings: Settings
) {
    companion object {
        private const val KEY_SHOW_WELCOME_VIDEO = "showWelcomeVideo"
    }

    fun getShowWelcomeVideo(): Boolean =
        settings.getBoolean(KEY_SHOW_WELCOME_VIDEO, defaultValue = true)

    fun setShowWelcomeVideo(value: Boolean) {
        settings.putBoolean(KEY_SHOW_WELCOME_VIDEO, value)
    }
}