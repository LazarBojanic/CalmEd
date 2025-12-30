package com.calmed.calmedbackend.util

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun Application.configureSerialization() {
	install(ContentNegotiation) {
		json(Util.Companion.jsonFormat())
	}
}
