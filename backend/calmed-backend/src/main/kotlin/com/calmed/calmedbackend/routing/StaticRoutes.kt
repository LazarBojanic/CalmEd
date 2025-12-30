package com.calmed.calmedbackend.routing

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing

fun Application.configureStaticRouting() {
	routing {
		staticResources("/", "static", index = "index.html")
		staticResources("/privacy-policy", "static/privacy-policy", index = "index.html")
	}
}