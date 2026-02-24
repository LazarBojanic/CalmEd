package com.calmed.calmedfrontendtourettes.reminders

import com.calmed.calmedfrontendtourettes.notifications.ReminderScheduler

actual class ReminderManager actual constructor() {

    actual fun enableMorningAndEvening() {
        ReminderScheduler.scheduleMorningAndEvening(
            context = androidAppContext,
            morningHour = 9,
            morningMinute = 0,
            eveningHour = 20,
            eveningMinute = 0
        )
    }

    actual fun disableMorningAndEvening() {
        ReminderScheduler.cancelMorningAndEvening(androidAppContext)
    }


}