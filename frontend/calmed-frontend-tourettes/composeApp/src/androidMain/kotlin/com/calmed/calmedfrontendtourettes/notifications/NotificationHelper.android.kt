package com.calmed.calmedfrontendtourettes.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calmed.calmedfrontendtourettes.R

object NotificationHelper {

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        text: String
    ) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w("NOTIFICATIONS", "POST_NOTIFICATIONS not granted; dropping notification id=$notificationId")
                return
            }
        }

        notificationManager.notify(notificationId, notification)
        Log.d("NOTIFICATIONS", "Notification shown id=$notificationId title=$title")
    }
}
