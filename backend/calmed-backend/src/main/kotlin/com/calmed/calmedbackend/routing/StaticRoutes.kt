package com.calmed.calmedbackend.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureStaticRouting() {
	routing {
		get("/ping") {
			call.respond(
				HttpStatusCode.OK,
				mapOf("message" to "pong")
			)
		}
		authenticate("auth-jwt") {
			staticFiles("/uploads", File("uploads"))
		}
	}
}
