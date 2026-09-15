package com.calmed.calmedtics.logging

import android.util.Log
import calmedtics.shared.BuildConfig

actual val isDevelopment: Boolean get() = BuildConfig.development

actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
	if (!isDevelopment && (level == LogLevel.DEBUG || level == LogLevel.INFO)) return
	when (level) {
		LogLevel.DEBUG -> if (throwable != null) Log.d(tag, message, throwable) else Log.d(tag, message)
		LogLevel.INFO -> if (throwable != null) Log.i(tag, message, throwable) else Log.i(tag, message)
		LogLevel.WARN -> if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
		LogLevel.ERROR -> if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
	}
}
