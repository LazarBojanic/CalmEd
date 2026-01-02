package com.calmed.calmedbackend.util

import kotlinx.serialization.json.Json

class Util {
	companion object {
		fun jsonFormat(): Json {
			return Json {
				prettyPrint = true
				isLenient = true
			}
		}
		fun printError(cause: Throwable) {
			println("========== ERROR ==========")
			println("Type: ${cause::class.simpleName}")
			println("Message: ${cause.message}")
			println("Stacktrace:")
			cause.printStackTrace()
			println("===========================")
		}
	}
}