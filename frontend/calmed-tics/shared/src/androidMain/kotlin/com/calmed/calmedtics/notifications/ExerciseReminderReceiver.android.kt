package com.calmed.calmedtics.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags

private val log = AppLog(LogTags.REMINDERS)

class ExerciseReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val title = intent.getStringExtra("title") ?: context.getString(com.calmed.calmedtics.shared.R.string.reminder_default_title)
        val text = intent.getStringExtra("text") ?: context.getString(com.calmed.calmedtics.shared.R.string.reminder_default_body)
        log.debug("Receiver fired id=$id title=$title")
        val notificationId = id.takeIf { it != -1 } ?: 2001

        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)


        NotificationHelper.showNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            text = text
        )


        if (hour != -1 && minute != -1) {
            ReminderScheduler.scheduleReminder(
                context = context,
                hour = hour,
                minute = minute,
                notificationId = notificationId,
                title = title,
                text = text
            )
        }
    }
}
