package com.calmed.calmedfrontendtourettes.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS_ID = "reminders"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            REMINDERS_ID,
            "Exercise reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Morning and evening exercise reminders"
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}