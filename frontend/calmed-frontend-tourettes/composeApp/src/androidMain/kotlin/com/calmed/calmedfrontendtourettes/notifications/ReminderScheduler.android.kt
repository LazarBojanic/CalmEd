package com.calmed.calmedfrontendtourettes.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

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
            title = "Morning exercise",
            text = "Time for your morning practice."
        )

        scheduleReminder(
            context = context,
            hour = eveningHour,
            minute = eveningMinute,
            notificationId = 4002,
            title = "Evening exercise",
            text = "Time for your evening practice."
        )
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
    }
}