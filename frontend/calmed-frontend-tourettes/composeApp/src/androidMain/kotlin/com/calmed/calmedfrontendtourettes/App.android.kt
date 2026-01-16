package com.calmed.calmedfrontendtourettes

import android.app.Application

class AndroidApp : Application() {
	override fun onCreate() {
		super.onCreate()
		val dev = true
		var url = ""
		if(dev){
			url = "http://127.0.0.1:8080"
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