package com.calmed.calmedbackend.routing

import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.resources.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
	install(Resources)

	routing {
		rateLimit(RateLimitName("auth")) {
			authRoutes()
		}
		rateLimit(RateLimitName("support")) {
			supportRoutes()
		}
		userRoutes()
		userInfoTicsRoutes()
		homeRoutes()
		programExerciseRoutes()
		exerciseGroupRoutes()
		paymentRoutes()
		userProgramRoutes()
		userExerciseProgressRoutes()
	}
}
