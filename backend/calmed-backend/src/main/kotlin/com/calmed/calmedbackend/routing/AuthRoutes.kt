package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IAuthService
import com.calmed.calmedbackend.service.specification.IMessageService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID
import kotlin.getValue

fun Route.authRoutes() {
	val authService by inject<IAuthService>()
	route("/auth/register") {
		post {
			val registerDto = call.receive<RegisterDto>()
			val tokenPairDto = authService.register(registerDto)
			call.respond(HttpStatusCode.OK, tokenPairDto)
		}
	}
	route("/auth/login") {
		post {
			val loginDto = call.receive<LoginDto>()
			val tokenPairDto = authService.login(loginDto)
			call.respond(HttpStatusCode.OK, tokenPairDto)
		}
	}
	route("/auth/refresh") {
		post {
			val refreshDto = call.receive<RefreshDto>()
			val tokenPairDto = authService.refresh(refreshDto)
			call.respond(HttpStatusCode.OK, tokenPairDto)
		}
	}
	authenticate("auth-jwt") {
		post("/auth/logout") {
			val principal = call.principal<JWTPrincipal>()!!
			val userId = UUID.fromString(principal.subject!!)
			authService.logout(userId)
			call.respond(HttpStatusCode.OK)
		}
	}
}