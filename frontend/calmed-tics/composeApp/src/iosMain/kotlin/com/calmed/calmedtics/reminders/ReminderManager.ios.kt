package com.calmed.calmedtics.reminders

import calmedtics.composeApp.BuildConfig
import com.calmed.calmedtics.settings.AppSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import platform.Foundation.NSDateComponents
import platform.Foundation.NSLog
import platform.UserNotifications.*

actual class ReminderManager actual constructor() : KoinComponent {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    actual fun enableMorningAndEvening() {
        requestPermissionIfNeeded()
        disableMorningAndEvening()

        if (BuildConfig.notificationDebug) {
            NSLog("REMINDERS: scheduling test notifications (10s, 20s)")
            // TEST: posle 10s i 20s notifikacija
            scheduleAfterSeconds(
                id = "test_morning_10s",
                seconds = 10,
                title = "TEST iOS 1",
                body = "Should arrive after 10 seconds"
            )
            scheduleAfterSeconds(
                id = "test_evening_20s",
                seconds = 20,
                title = "TEST iOS 2",
                body = "Should arrive after 20 seconds"
            )
        } else {
            val appSettings: AppSettings = get()
            val morningTime = appSettings.getMorningReminderTime().split(":")
            val eveningTime = appSettings.getEveningReminderTime().split(":")

            val mHour = morningTime.getOrNull(0)?.toIntOrNull() ?: 9
            val mMin = morningTime.getOrNull(1)?.toIntOrNull() ?: 0
            val eHour = eveningTime.getOrNull(0)?.toIntOrNull() ?: 20
            val eMin = eveningTime.getOrNull(1)?.toIntOrNull() ?: 0

            NSLog("REMINDERS: scheduling daily notifications $mHour:$mMin and $eHour:$eMin")
            scheduleDaily(
                id = "morning_reminder",
                hour = mHour,
                minute = mMin,
                title = "Morning exercise",
                body = "Time for your morning practice."
            )
            scheduleDaily(
                id = "evening_reminder",
                hour = eHour,
                minute = eMin,
                title = "Evening exercise",
                body = "Time for your evening practice."
            )
        }

    }

    actual fun disableMorningAndEvening() {
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf("morning_reminder", "evening_reminder", "test_morning_10s", "test_evening_20s")
        )
        center.removeDeliveredNotificationsWithIdentifiers(
            listOf("morning_reminder", "evening_reminder", "test_morning_10s", "test_evening_20s")
        )
        NSLog("REMINDERS: cleared pending and delivered reminders")
    }

    private fun requestPermissionIfNeeded() {
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (error != null) {
            NSLog("REMINDERS: permission error ${error.localizedDescription ?: "unknown"}")
        } else {
            NSLog("REMINDERS: permission granted=${if (granted) "true" else "false"}")
        }
        }
    }

    private fun scheduleDaily(
        id: String,
        hour: Int,
        minute: Int,
        title: String,
        body: String
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }

        val dateComponents = NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(minute.toLong())
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { _ -> }
        NSLog("REMINDERS: scheduled daily id=$id at ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
    }
    private fun scheduleAfterSeconds(
        id: String,
        seconds: Int,
        title: String,
        body: String
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = seconds.toDouble(),
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { _ -> }
        NSLog("REMINDERS: scheduled test id=$id in ${seconds}s")
    }
}
