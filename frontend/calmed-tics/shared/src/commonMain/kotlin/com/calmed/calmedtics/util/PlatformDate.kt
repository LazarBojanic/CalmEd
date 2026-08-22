package com.calmed.calmedtics.util

data class Ymd(val year: Int, val month: Int, val day: Int)

expect fun currentYmd(): Ymd
expect fun currentTimeMillis(): Long

// ISO-8601 to epoch day, same as java.time.LocalDate.toEpochDay
fun dateToEpochDay(year: Int, month: Int, day: Int): Long {
    var y = year.toLong()
    val m = month.toLong()
    y -= if (m <= 2) 1 else 0
    val era = if (y >= 0) y / 400 else (y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097L + doe - 719468L
}

fun epochDayToYmd(epochDay: Long): Ymd {
    var zeroDay = epochDay + 719468
    zeroDay -= if (zeroDay < 0) (zeroDay + 1) / 146097 * 146097 - 146097 else 0
    val era = zeroDay / 146097
    val doe = zeroDay - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + if (mp < 10) 3 else -9
    return Ymd((y + if (m <= 2) 1 else 0).toInt(), m.toInt(), d.toInt())
}
