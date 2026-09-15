package com.calmed.calmedtics.reminders

import calmedtics.shared.BuildConfig
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.settings.AppSettings
import platform.Foundation.NSBundle
import platform.Foundation.NSDateComponents
import platform.UserNotifications.*

class IosReminderManager(
    private val appSettings: AppSettings,
) : ReminderManager {

    private val center = UNUserNotificationCenter.currentNotificationCenter()
    private val log = AppLog(LogTags.REMINDERS)

    private fun localized(key: String): String =
        NSBundle.mainBundle.localizedStringForKey(key, null, null)

    override fun enableMorningAndEvening() {
        requestPermissionIfNeeded()
        disableMorningAndEvening()

        if (BuildConfig.notificationDebug) {
            log.debug("scheduling test notifications (10s, 20s)")
            scheduleAfterSeconds(
                id = "test_morning_10s",
                seconds = 10,
                title = localized("reminder_test_title_1"),
                body = localized("reminder_test_body_10s")
            )
            scheduleAfterSeconds(
                id = "test_evening_20s",
                seconds = 20,
                title = localized("reminder_test_title_2"),
                body = localized("reminder_test_body_20s")
            )
        } else {
            val morningTime = appSettings.getMorningReminderTime().split(":")
            val eveningTime = appSettings.getEveningReminderTime().split(":")

            val mHour = morningTime.getOrNull(0)?.toIntOrNull() ?: 9
            val mMin = morningTime.getOrNull(1)?.toIntOrNull() ?: 0
            val eHour = eveningTime.getOrNull(0)?.toIntOrNull() ?: 20
            val eMin = eveningTime.getOrNull(1)?.toIntOrNull() ?: 0

            log.debug("scheduling daily notifications $mHour:$mMin and $eHour:$eMin")
            scheduleDaily(
                id = "morning_reminder",
                hour = mHour,
                minute = mMin,
                title = localized("reminder_morning_title"),
                body = localized("reminder_morning_body")
            )
            scheduleDaily(
                id = "evening_reminder",
                hour = eHour,
                minute = eMin,
                title = localized("reminder_evening_title"),
                body = localized("reminder_evening_body")
            )
        }

    }

    override fun disableMorningAndEvening() {
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf("morning_reminder", "evening_reminder", "test_morning_10s", "test_evening_20s")
        )
        center.removeDeliveredNotificationsWithIdentifiers(
            listOf("morning_reminder", "evening_reminder", "test_morning_10s", "test_evening_20s")
        )
        log.debug("cleared pending and delivered reminders")
    }

    private fun requestPermissionIfNeeded() {
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (error != null) {
                log.warn("permission error ${error.localizedDescription ?: "unknown"}")
            } else {
                log.debug("permission granted=${if (granted) "true" else "false"}")
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
        log.debug("scheduled daily id=$id at ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
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
        log.debug("scheduled test id=$id in ${seconds}s")
    }
}
