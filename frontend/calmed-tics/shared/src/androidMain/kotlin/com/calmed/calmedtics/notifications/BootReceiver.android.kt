package com.calmed.calmedtics.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.reminders.ReminderManager
import com.calmed.calmedtics.settings.AppSettings

private val log = AppLog(LogTags.REMINDERS)

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        runCatching {
            val appSettings = org.koin.core.context.GlobalContext.get().get<AppSettings>()
            if (appSettings.isRemindersEnabled()) {
                org.koin.core.context.GlobalContext.get().get<ReminderManager>().enableMorningAndEvening()
            }
        }.onFailure {
            log.warn("Unable to reschedule reminders after boot", it)
        }
    }
}