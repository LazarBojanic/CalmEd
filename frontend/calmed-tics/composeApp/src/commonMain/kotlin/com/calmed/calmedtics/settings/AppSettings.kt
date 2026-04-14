package com.calmed.calmedtics.settings

import com.russhwolf.settings.Settings

class AppSettings(
    private val settings: Settings
) {
    companion object {
        private const val KEY_SHOW_WELCOME_VIDEO = "showWelcomeVideo"
        private const val KEY_SHOW_COURSE_OVERVIEW = "showCourseOverview"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_MORNING_REMINDER_TIME = "morning_reminder_time"
        private const val KEY_EVENING_REMINDER_TIME = "evening_reminder_time"
    }

    private fun welcomeVideoKey(userId: String?): String {
        val suffix = userId?.takeIf { it.isNotBlank() }
        return if (suffix == null) KEY_SHOW_WELCOME_VIDEO else "$KEY_SHOW_WELCOME_VIDEO:$suffix"
    }

    private fun courseOverviewKey(userId: String?): String {
        val suffix = userId?.takeIf { it.isNotBlank() }
        return if (suffix == null) KEY_SHOW_COURSE_OVERVIEW else "$KEY_SHOW_COURSE_OVERVIEW:$suffix"
    }

    fun getShowWelcomeVideo(userId: String?): Boolean =
        settings.getBoolean(welcomeVideoKey(userId), defaultValue = true)


    fun setShowWelcomeVideo(userId: String?, value: Boolean) {
        settings.putBoolean(welcomeVideoKey(userId), value)
    }

    fun getShowCourseOverview(userId: String?): Boolean =
        settings.getBoolean(courseOverviewKey(userId), defaultValue = true)

    fun setShowCourseOverview(userId: String?, value: Boolean) {
        settings.putBoolean(courseOverviewKey(userId), value)
    }
    fun isRemindersEnabled(): Boolean =
        settings.getBoolean(KEY_REMINDERS_ENABLED, false)

    fun setRemindersEnabled(value: Boolean) {
        settings.putBoolean(KEY_REMINDERS_ENABLED, value)
    }
    fun getAppLanguage(): String =
        settings.getString(KEY_APP_LANGUAGE, "en")

    fun setAppLanguage(value: String) {
        settings.putString(KEY_APP_LANGUAGE, value)
    }
    fun getMorningReminderTime(): String = settings.getString(KEY_MORNING_REMINDER_TIME, "08:00")
    fun setMorningReminderTime(value: String) = settings.putString(KEY_MORNING_REMINDER_TIME, value)

    fun getEveningReminderTime(): String = settings.getString(KEY_EVENING_REMINDER_TIME, "17:00")
    fun setEveningReminderTime(value: String) = settings.putString(KEY_EVENING_REMINDER_TIME, value)
}