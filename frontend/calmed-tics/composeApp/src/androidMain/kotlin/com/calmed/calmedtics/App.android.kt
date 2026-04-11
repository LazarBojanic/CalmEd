package com.calmed.calmedtics

import android.app.Application
import com.calmed.calmedtics.di.appContext
import calmedtics.composeApp.BuildConfig
import com.calmed.calmedtics.notifications.NotificationChannels


class AndroidApp : Application() {
	override fun onCreate() {
		super.onCreate()
		NotificationChannels.create(this)
		com.calmed.calmedtics.reminders.androidAppContext = applicationContext
		appContext = this
		var url = ""
		if(BuildConfig.development){
			if(BuildConfig.adbReverse){
				url = "http://127.0.0.1:8080"
			}
			else{
				url = "http://10.0.2.2:8080"
			}
		}
		else{
			url = "https://api.calm-ed.com"
		}
		initKoin(
			baseUrl = url,
			androidModule(this)
		)
	}
}