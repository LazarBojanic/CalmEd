package com.calmed.calmedfrontendtourettes.util

import java.util.Calendar

actual fun currentYmd(): Ymd {
    val cal = Calendar.getInstance()
    return Ymd(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH) + 1,
        day = cal.get(Calendar.DAY_OF_MONTH)
    )
}