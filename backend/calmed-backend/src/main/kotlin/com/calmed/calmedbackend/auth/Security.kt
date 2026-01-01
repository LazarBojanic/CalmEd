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
			realm = "Protected"
			verifier(authService.accessVerifier())

			validate { cred ->
				if (cred.payload.getClaim("typ").asString() != TokenType.ACCESS.name) {
					return@validate null
				}
				JWTPrincipal(cred.payload)
			}
		}
	}
}
