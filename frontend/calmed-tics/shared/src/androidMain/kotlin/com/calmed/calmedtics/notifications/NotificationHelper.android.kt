package com.calmed.calmedtics.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint

object NotificationHelper {

    const val EXTRA_OPEN_HOME = "open_home_from_notification"

    @SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        text: String
    ) {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                putExtra(EXTRA_OPEN_HOME, true)
            }

        if (launchIntent == null) {
            Log.e(
                "NOTIFICATIONS",
                "Could not create launch intent for package ${context.packageName}"
            )
            return
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = context.applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_dialog_info

        val notification =
            NotificationCompat.Builder(
                context,
                NotificationChannels.REMINDERS_ID
            )
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                Log.w(
                    "NOTIFICATIONS",
                    "POST_NOTIFICATIONS not granted; dropping notification id=$notificationId"
                )
                return
            }
        }

        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)

        Log.d(
            "NOTIFICATIONS",
            "Notification shown id=$notificationId title=$title"
        )
    }
}