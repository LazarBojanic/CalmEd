package com.calmed.calmedbackend.routing

import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
	install(Resources)

	routing {
		authRoutes()
		authenticate("auth-jwt") {
			messageRoutes()
		}
	}
}
