package com.calmed.calmedtics.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calmed.calmedtics.reminders.ReminderManager
import com.calmed.calmedtics.reminders.androidAppContext
import com.calmed.calmedtics.settings.AppSettings



class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        androidAppContext = context.applicationContext
        val appSettings = org.koin.core.context.GlobalContext.get().get<AppSettings>()
        if (appSettings.isRemindersEnabled()) {
            ReminderManager().enableMorningAndEvening()
        }
    }
}