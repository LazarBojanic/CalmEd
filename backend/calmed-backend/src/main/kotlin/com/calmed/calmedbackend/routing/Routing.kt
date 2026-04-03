package com.calmed.calmedbackend.routing

import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
	install(Resources)

	routing {
		authRoutes()
		userRoutes()
		userInfoTicsRoutes()
		homeRoutes()
		programExerciseRoutes()
		paymentRoutes()
		userProgramRoutes()
		userExerciseProgressRoutes()
		supportRoutes()
	}
}
