package com.calmed.calmedtics.reminders

import android.content.Context
import calmedtics.shared.BuildConfig
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.notifications.ReminderScheduler
import com.calmed.calmedtics.notifications.requestNotificationPermissionIfNeeded
import com.calmed.calmedtics.settings.AppSettings

class AndroidReminderManager(
    private val context: Context,
    private val appSettings: AppSettings,
) : ReminderManager {

    private val log = AppLog(LogTags.REMINDERS)

    override fun enableMorningAndEvening() {
        val permissionGranted = requestNotificationPermissionIfNeeded(context)
        log.debug("enableMorningAndEvening permissionGranted=$permissionGranted debug=${BuildConfig.notificationDebug}")

        val morningTime = appSettings.getMorningReminderTime().split(":")
        val eveningTime = appSettings.getEveningReminderTime().split(":")

        val mHour = morningTime.getOrNull(0)?.toIntOrNull() ?: 9
        val mMin = morningTime.getOrNull(1)?.toIntOrNull() ?: 0
        val eHour = eveningTime.getOrNull(0)?.toIntOrNull() ?: 20
        val eMin = eveningTime.getOrNull(1)?.toIntOrNull() ?: 0

        if (BuildConfig.notificationDebug) {
            ReminderScheduler.scheduleTestReminders(
                context = context
            )
        } else {
            ReminderScheduler.scheduleMorningAndEvening(
                context = context,
                morningHour = mHour,
                morningMinute = mMin,
                eveningHour = eHour,
                eveningMinute = eMin
            )
        }
    }

    override fun disableMorningAndEvening() {
        ReminderScheduler.cancelMorningAndEvening(context)
    }
}
