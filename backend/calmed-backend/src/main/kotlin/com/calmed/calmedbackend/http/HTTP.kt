package com.calmed.calmedbackend.http

import com.calmed.calmedbackend.config.KtorConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.ratelimit.*
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.seconds

fun Application.configureHTTP() {
	val ktorConfig by inject<KtorConfig>()
	install(Compression)
	install(RateLimit) {
		register(RateLimitName("auth")) {
			rateLimiter(limit = 20, refillPeriod = 60.seconds)
		}
		register(RateLimitName("support")) {
			rateLimiter(limit = 5, refillPeriod = 60.seconds)
		}
	}
	install(CORS) {
		allowMethod(HttpMethod.Options)
		allowMethod(HttpMethod.Get)
		allowMethod(HttpMethod.Post)
		allowMethod(HttpMethod.Put)
		allowMethod(HttpMethod.Delete)
		allowMethod(HttpMethod.Patch)

		allowHeader(HttpHeaders.ContentType)
		allowHeader(HttpHeaders.Authorization)

		allowCredentials = false

		allowHost("appleid.apple.com", listOf("https"))

		allowHost("api.calm-ed.com", listOf("https"))
		allowHost("api.calm-ed.org", listOf("https"))
		allowHost("api.calm-ed.net", listOf("https"))
		allowHost("api.calm-ed.edu", listOf("https"))
		allowHost("calm-ed.com", listOf("https"))
		allowHost("calm-ed.org", listOf("https"))
		allowHost("calm-ed.net", listOf("https"))
		allowHost("calm-ed.edu", listOf("https"))
		allowHost("mediumseagreen-goat-237667.hostingersite.com", schemes = listOf("https"))

		if(ktorConfig.development){
			allowHost("localhost:3000", listOf("http"))
			allowHost("localhost:8080", listOf("http"))
			allowHost("127.0.0.1:3000", listOf("http"))
			allowHost("127.0.0.1:8080", listOf("http"))
			allowHost("hoppscotch.io", listOf("https"))
		}
	}
	install(DefaultHeaders) {
		header("X-Engine", "Ktor")
		header("X-Content-Type-Options", "nosniff")
		header("X-Frame-Options", "DENY")
		header("Referrer-Policy", "no-referrer")
		header("Permissions-Policy", "camera=(), microphone=(), geolocation=()")
		if (!ktorConfig.development) {
			header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
		}
	}
}
