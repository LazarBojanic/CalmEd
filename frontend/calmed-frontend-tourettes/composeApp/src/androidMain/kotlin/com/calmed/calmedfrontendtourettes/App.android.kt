package com.calmed.calmedfrontendtourettes

import android.app.Application

class AndroidApp : Application() {
	override fun onCreate() {
		super.onCreate()
		val dev = true
		var url = ""
		if(dev){
			url = "http://10.0.2.2:8080"
		}
		else{
			url = "https://srv1092316.hstgr.cloud"
		}
		initKoin(
			baseUrl = url,
			androidModule(this)
		)
	}
}