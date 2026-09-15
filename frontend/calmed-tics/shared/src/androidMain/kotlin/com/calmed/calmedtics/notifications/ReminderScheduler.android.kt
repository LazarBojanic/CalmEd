package com.calmed.calmedtics.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.shared.R
import java.util.Calendar

private val log = AppLog(LogTags.REMINDERS)

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        hour: Int,
        minute: Int,
        notificationId: Int,
        title: String,
        text: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ExerciseReminderReceiver::class.java).apply {
            putExtra("id", notificationId)
            putExtra("title", title)
            putExtra("text", text)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
        log.debug(
            "Scheduled daily id=$notificationId at ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        )
    }

    fun scheduleMorningAndEvening(
        context: Context,
        morningHour: Int,
        morningMinute: Int,
        eveningHour: Int,
        eveningMinute: Int
    ) {
        scheduleReminder(
            context = context,
            hour = morningHour,
            minute = morningMinute,
            notificationId = 4001,
            title = context.getString(R.string.reminder_morning_title),
            text = context.getString(R.string.reminder_morning_body)
        )

        scheduleReminder(
            context = context,
            hour = eveningHour,
            minute = eveningMinute,
            notificationId = 4002,
            title = context.getString(R.string.reminder_evening_title),
            text = context.getString(R.string.reminder_evening_body)
        )
    }

    fun scheduleTestReminders(
        context: Context,
        firstDelaySeconds: Int = 10,
        secondDelaySeconds: Int = 20
    ) {
        scheduleReminderAfterSeconds(
            context = context,
            seconds = firstDelaySeconds,
            notificationId = 4101,
            title = context.getString(R.string.reminder_test_title_1),
            text = context.getString(R.string.reminder_test_body_delay, firstDelaySeconds)
        )

        scheduleReminderAfterSeconds(
            context = context,
            seconds = secondDelaySeconds,
            notificationId = 4102,
            title = context.getString(R.string.reminder_test_title_2),
            text = context.getString(R.string.reminder_test_body_delay, secondDelaySeconds)
        )
        log.debug("Scheduled test reminders after ${firstDelaySeconds}s and ${secondDelaySeconds}s")
    }

    fun cancelReminder(context: Context, notificationId: Int) {
        val intent = Intent(context, ExerciseReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    fun cancelMorningAndEvening(context: Context) {
        cancelReminder(context, 4001)
        cancelReminder(context, 4002)
        cancelReminder(context, 4101)
        cancelReminder(context, 4102)
    }

    private fun scheduleReminderAfterSeconds(
        context: Context,
        seconds: Int,
        notificationId: Int,
        title: String,
        text: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ExerciseReminderReceiver::class.java).apply {
            putExtra("id", notificationId)
            putExtra("title", title)
            putExtra("text", text)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + seconds * 1000L
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        log.debug("Scheduled test id=$notificationId in ${seconds}s")
    }
}
