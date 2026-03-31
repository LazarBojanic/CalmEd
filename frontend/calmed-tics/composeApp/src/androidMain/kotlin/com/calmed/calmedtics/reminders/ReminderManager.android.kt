package com.calmed.calmedtics.reminders

import android.util.Log
import calmedtics.composeApp.BuildConfig
import com.calmed.calmedtics.notifications.ReminderScheduler
import com.calmed.calmedtics.notifications.requestNotificationPermissionIfNeeded

actual class ReminderManager actual constructor() {

    actual fun enableMorningAndEvening() {
        val permissionGranted = requestNotificationPermissionIfNeeded(androidAppContext)
        Log.d("REMINDERS", "enableMorningAndEvening permissionGranted=$permissionGranted debug=${BuildConfig.notificationDebug}")

        if (BuildConfig.notificationDebug) {
            ReminderScheduler.scheduleTestReminders(
                context = androidAppContext
            )
        } else {
            ReminderScheduler.scheduleMorningAndEvening(
                context = androidAppContext,
                morningHour = 9,
                morningMinute = 0,
                eveningHour = 20,
                eveningMinute = 0
            )
        }
    }

    actual fun disableMorningAndEvening() {
        ReminderScheduler.cancelMorningAndEvening(androidAppContext)
    }


}
