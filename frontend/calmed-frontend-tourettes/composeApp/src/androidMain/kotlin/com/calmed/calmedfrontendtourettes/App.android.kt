package com.calmed.calmedfrontendtourettes

import android.app.Application
import com.calmed.calmedfrontendtourettes.di.appContext
import calmedfrontendtourettes.composeApp.BuildConfig
import com.calmed.calmedfrontendtourettes.notifications.NotificationChannels


class AndroidApp : Application() {
	override fun onCreate() {
		super.onCreate()
		NotificationChannels.create(this)
		com.calmed.calmedfrontendtourettes.reminders.androidAppContext = applicationContext
		appContext = this
		var url = ""
		if(BuildConfig.development){
			if(BuildConfig.adbReverse){
				url = "http://192.168.0.35:8080"
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