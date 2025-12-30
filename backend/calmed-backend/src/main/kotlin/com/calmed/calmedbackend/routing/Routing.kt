package com.calmed.calmedbackend.routing

import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
	install(Resources)

	routing {
		messageRoutes()
	}
}

