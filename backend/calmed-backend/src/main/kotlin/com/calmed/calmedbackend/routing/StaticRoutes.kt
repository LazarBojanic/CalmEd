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
import io.ktor.server.response.respondRedirect
import io.ktor.server.http.content.staticFiles
import java.io.File

fun Application.configureStaticRouting() {
	val googleOAuthConfig by inject<GoogleOAuthConfig>()
	val appleConfig by inject<AppleConfig>()

	routing {
		staticResources("/", "static", index = "index.html")
		get("/home") { call.respondRedirect("/") }
		staticResources("/about", "static", index = "about.html")
		get("/payment") { call.respondRedirect("/account/payments") }
		staticResources("/account", "static", index = "account.html")
		staticResources("/account/payments", "static", index = "payment.html")
		staticResources("/login", "static", index = "login.html")
		staticResources("/register", "static", index = "register.html")
		staticResources("/privacy-policy", "static/privacy-policy", index = "index.html")
		staticResources("/js", "static/js")
		staticFiles("/uploads", File("uploads"))

		route("/config") {
			get {
				call.respond(mapOf(
					"googleClientId" to googleOAuthConfig.webClientId,
					"appleClientId" to appleConfig.clientId,
					"appleRedirectUri" to appleConfig.redirectURI,
					"appleRedirectUriWeb" to "https://api.calm-ed.com/auth/apple/callback/web"
				))
			}
		}
	}
}