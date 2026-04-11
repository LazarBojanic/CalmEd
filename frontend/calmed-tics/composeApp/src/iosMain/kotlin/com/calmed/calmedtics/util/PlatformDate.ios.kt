package com.calmed.calmedtics.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentYmd(): Ymd {
	val now = NSDate()
	val calendar = NSCalendar.currentCalendar
	val components = calendar.components(
		NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
		fromDate = now
	)

	return Ymd(
		year = components.year.toInt(),
		month = components.month.toInt(),
		day = components.day.toInt()
	)
}

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
