package com.calmed.calmedbackend.util

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.util.*
import org.slf4j.event.*

private val requestStartedAtKey = AttributeKey<Long>("calmed.request.startedAt")

fun Application.configureMonitoring() {
	intercept(ApplicationCallPipeline.Setup) {
		call.attributes.put(requestStartedAtKey, System.nanoTime())
	}
	install(CallLogging) {
		level = Level.INFO
		filter { call -> call.request.path().startsWith("/") }
		format { call ->
			val status = call.response.status()?.value ?: 0
			val startedAt = call.attributes.getOrNull(requestStartedAtKey)
			val durationMs = startedAt?.let { (System.nanoTime() - it) / 1_000_000 }
			"${call.request.httpMethod.value} ${call.request.path()} -> $status (${durationMs ?: "-"}ms)"
		}
	}
}
