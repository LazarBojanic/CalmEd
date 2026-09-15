package com.calmed.calmedtics.logging

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

expect val isDevelopment: Boolean

expect fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?)

class AppLog(private val tag: String) {
	fun debug(message: String, throwable: Throwable? = null) =
		platformLog(LogLevel.DEBUG, tag, message, throwable)

	fun info(message: String, throwable: Throwable? = null) =
		platformLog(LogLevel.INFO, tag, message, throwable)

	fun warn(message: String, throwable: Throwable? = null) =
		platformLog(LogLevel.WARN, tag, message, throwable)

	fun error(message: String, throwable: Throwable? = null) =
		platformLog(LogLevel.ERROR, tag, message, throwable)
}

object LogTags {
	const val APP = "APP"
	const val AUTH = "AUTH"
	const val APPLE_AUTH = "APPLE_AUTH"
	const val GOOGLE_AUTH = "GOOGLE_AUTH"
	const val BILLING = "BILLING"
	const val CAST = "CAST"
	const val NOTIFICATIONS = "NOTIFICATIONS"
	const val REMINDERS = "REMINDERS"
	const val VIDEO = "VIDEO"
	const val NETWORK = "NETWORK"
	const val DATABASE = "DATABASE"
}
