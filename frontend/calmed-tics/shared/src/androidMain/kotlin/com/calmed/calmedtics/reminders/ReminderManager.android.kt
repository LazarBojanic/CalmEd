package com.calmed.calmedtics.reminders

import android.util.Log
import calmedtics.shared.BuildConfig
import com.calmed.calmedtics.notifications.ReminderScheduler
import com.calmed.calmedtics.notifications.requestNotificationPermissionIfNeeded

actual class ReminderManager actual constructor() {

    actual fun enableMorningAndEvening() {
        val permissionGranted = requestNotificationPermissionIfNeeded(androidAppContext)
        Log.d("REMINDERS", "enableMorningAndEvening permissionGranted=$permissionGranted debug=${BuildConfig.notificationDebug}")

        val appSettings = org.koin.core.context.GlobalContext.get().get<com.calmed.calmedtics.settings.AppSettings>()
        val morningTime = appSettings.getMorningReminderTime().split(":")
        val eveningTime = appSettings.getEveningReminderTime().split(":")
        
        val mHour = morningTime.getOrNull(0)?.toIntOrNull() ?: 9
        val mMin = morningTime.getOrNull(1)?.toIntOrNull() ?: 0
        val eHour = eveningTime.getOrNull(0)?.toIntOrNull() ?: 20
        val eMin = eveningTime.getOrNull(1)?.toIntOrNull() ?: 0

        if (BuildConfig.notificationDebug) {
            ReminderScheduler.scheduleTestReminders(
                context = androidAppContext
            )
        } else {
            ReminderScheduler.scheduleMorningAndEvening(
                context = androidAppContext,
                morningHour = mHour,
                morningMinute = mMin,
                eveningHour = eHour,
                eveningMinute = eMin
            )
        }
    }

    actual fun disableMorningAndEvening() {
        ReminderScheduler.cancelMorningAndEvening(androidAppContext)
    }


}
