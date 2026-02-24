package com.calmed.calmedfrontendtourettes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calmed.calmedfrontendtourettes.reminders.ReminderManager
import com.calmed.calmedfrontendtourettes.reminders.androidAppContext
import com.calmed.calmedfrontendtourettes.settings.AppSettings



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