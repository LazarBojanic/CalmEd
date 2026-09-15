package com.calmed.calmedbackend.util

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class Util {
	companion object {
		private val logger = LoggerFactory.getLogger(Util::class.java)

		fun jsonFormat(): Json {
			return Json {
				prettyPrint = true
				isLenient = true
				encodeDefaults = true
			}
		}
		fun printError(cause: Throwable) {
			logger.error("Request failed: {}", cause.message, cause)
		}
	}
}