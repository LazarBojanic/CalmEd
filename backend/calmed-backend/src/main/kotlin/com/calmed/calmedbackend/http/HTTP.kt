package com.calmed.calmedbackend.http

import com.calmed.calmedbackend.config.KtorConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import org.koin.ktor.ext.inject

fun Application.configureHTTP() {
	val ktorConfig by inject<KtorConfig>()
	install(Compression)
	install(CORS) {
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

		allowHost("api.calm-ed.com", listOf("https"))
		allowHost("api.calm-ed.org", listOf("https"))
		allowHost("api.calm-ed.net", listOf("https"))
		allowHost("api.calm-ed.edu", listOf("https"))
		allowHost("calm-ed.com", listOf("https"))
		allowHost("calm-ed.org", listOf("https"))
		allowHost("calm-ed.net", listOf("https"))
		allowHost("calm-ed.edu", listOf("https"))

		if(ktorConfig.development){
			allowHost("localhost:3000", listOf("http"))
			allowHost("localhost:8080", listOf("http"))
			allowHost("127.0.0.1:3000", listOf("http"))
			allowHost("127.0.0.1:8080", listOf("http"))
			allowHost("hoppscotch.io", listOf("https"))
		}
	}
	install(DefaultHeaders) {
		header("X-Engine", "Ktor") // will send this header with each response
	}
}
