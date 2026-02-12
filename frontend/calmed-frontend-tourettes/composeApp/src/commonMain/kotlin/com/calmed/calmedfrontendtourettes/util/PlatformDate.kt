package com.calmed.calmedfrontendtourettes.util

data class Ymd(val year: Int, val month: Int, val day: Int)

expect fun currentYmd(): Ymd
