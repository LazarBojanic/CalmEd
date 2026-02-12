package com.calmed.calmedfrontendtourettes.util

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
