package com.calmed.calmedfrontendtourettes.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okio.ByteString.Companion.decodeBase64

fun jwtDecode(token: String): JsonObject {
	val payload = token.split(".")[1]
	val decoded = payload.decodeBase64()!!.utf8()
	return Json.parseToJsonElement(decoded).jsonObject
}
