package com.calmed.calmedfrontendtourettes

import android.app.Application
import com.calmed.calmedfrontendtourettes.di.appContext
import calmedfrontendtourettes.composeApp.BuildConfig

class AndroidApp : Application() {
	override fun onCreate() {
		super.onCreate()
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