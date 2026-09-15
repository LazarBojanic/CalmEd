package com.calmed.calmedtics.logging

import calmedtics.shared.BuildConfig
import platform.Foundation.NSLog

actual val isDevelopment: Boolean get() = BuildConfig.development

actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
	if (!isDevelopment && (level == LogLevel.DEBUG || level == LogLevel.INFO)) return
	val suffix = throwable?.let { " | $it" } ?: ""
	NSLog("[$tag][$level] $message$suffix")
}
