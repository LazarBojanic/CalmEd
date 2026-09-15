package com.calmed.calmedtics

import android.os.Build

class AndroidPlatform : Platform {
	override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val appBaseUrl: String
	get() = when {
		!calmedtics.shared.BuildConfig.development -> calmedtics.shared.BuildConfig.baseUrl
		calmedtics.shared.BuildConfig.adbReverse -> "http://127.0.0.1:8080"
		else -> "http://10.0.2.2:8080"
	}