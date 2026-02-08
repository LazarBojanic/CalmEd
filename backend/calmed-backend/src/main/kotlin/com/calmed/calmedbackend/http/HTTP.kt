package com.calmed.calmedbackend.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {
	install(Compression)
	install(CORS) {
		val dev = true;
		allowMethod(HttpMethod.Options)
		allowMethod(HttpMethod.Get)
		allowMethod(HttpMethod.Post)
		allowMethod(HttpMethod.Put)
		allowMethod(HttpMethod.Delete)
		allowMethod(HttpMethod.Patch)

		allowHeader(HttpHeaders.ContentType)
		allowHeader(HttpHeaders.Authorization)

		allowCredentials = true

		// Apple OAuth form_post: browser POSTs from Origin https://appleid.apple.com. CORS rejects unknown origins with 403.
		allowHost("appleid.apple.com", listOf("https"))

		if(dev){
			allowHost("localhost:3000", listOf("http"))
			allowHost("localhost:8080", listOf("http"))
			allowHost("127.0.0.1:3000", listOf("http"))
			allowHost("127.0.0.1:8080", listOf("http"))
			allowHost("hoppscotch.io", listOf("https"))
			allowHost("appleid.apple.com", listOf("https"))
		}
	}
	install(DefaultHeaders) {
		header("X-Engine", "Ktor") // will send this header with each response
	}
}
