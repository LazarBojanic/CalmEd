package com.calmed.calmedtics

import android.app.Application
import calmedtics.shared.BuildConfig
import com.calmed.calmedtics.notifications.NotificationChannels

class AndroidApp : Application() {
	override fun onCreate() {
		super.onCreate()
		NotificationChannels.create(this)
		initKoin(
			baseUrl = appBaseUrl,
			development = BuildConfig.development,
			androidModule(this)
		)
	}
}
