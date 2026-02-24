package com.calmed.calmedfrontendtourettes.reminders

import platform.Foundation.NSDateComponents
import platform.UserNotifications.*

actual class ReminderManager actual constructor() {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    actual fun enableMorningAndEvening() {
        requestPermissionIfNeeded()
        disableMorningAndEvening()

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

        // PRODUCTION (vrati posle):
        // scheduleDaily(id="morning_reminder", hour=9, minute=0, ...)
        // scheduleDaily(id="evening_reminder", hour=20, minute=0, ...)
    }

    actual fun disableMorningAndEvening() {
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf("morning_reminder", "evening_reminder", "test_morning_10s", "test_evening_20s")
        )
        center.removeDeliveredNotificationsWithIdentifiers(
            listOf("morning_reminder", "evening_reminder", "test_morning_10s", "test_evening_20s")
        )
    }

    private fun requestPermissionIfNeeded() {
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { _, _ -> }
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
    }
}