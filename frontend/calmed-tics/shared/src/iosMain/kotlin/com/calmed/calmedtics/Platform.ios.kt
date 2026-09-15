package com.calmed.calmedtics

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
	override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual val appBaseUrl: String
	get() = if (calmedtics.shared.BuildConfig.development) {
		"http://127.0.0.1:8080"
	} else {
		calmedtics.shared.BuildConfig.baseUrl
	}