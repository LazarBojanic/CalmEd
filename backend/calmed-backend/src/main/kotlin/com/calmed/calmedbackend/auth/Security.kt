package com.calmed.calmedbackend.auth

import com.calmed.calmedbackend.model.dto.response.ErrorDto
import com.calmed.calmedbackend.service.specification.IAuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
	val authService by inject<IAuthService>()

	install(Authentication) {
		jwt("auth-jwt") {
			realm = "Access to protected endpoints"
			verifier(authService.accessVerifier())

			validate { cred ->
				// require subject
				val sub = cred.payload.subject ?: return@validate null

				// explicit enforcement: token must be ACCESS (not REFRESH)
				val typClaim = try {
					cred.payload.getClaim("typ").asString()
				} catch (_: Exception) {
					null
				} ?: return@validate null

				if (typClaim != TokenType.ACCESS.name) return@validate null

				JWTPrincipal(cred.payload)
			}

			challenge { _, _ ->
				call.respond(
					HttpStatusCode.Unauthorized,
					ErrorDto("Invalid or expired access token")
				)
			}
		}
	}
}
