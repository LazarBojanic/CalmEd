package com.calmed.calmedbackend.routing

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import com.calmed.calmedbackend.config.GoogleOAuthConfig
import com.calmed.calmedbackend.config.AppleConfig
import com.calmed.calmedbackend.model.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondRedirect
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import java.io.File

fun Application.configureStaticRouting() {
	routing {
		get("/ping"){
			call.respond(
				HttpStatusCode.OK,
				mapOf("message" to "pong")
			)
		}
		staticFiles("/uploads", File("uploads"))
	}
}