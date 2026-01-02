package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.service.specification.IAuthService
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

fun Route.authRoutes() {
	val authService by inject<IAuthService>()

	route("/auth/register") {
		post {
			val registerDto = call.receive<RegisterDto>()
			val tokenPairDto = authService.register(registerDto)

			when (tokenPairDto) {
				is AppResult.Success -> {
					call.respond(HttpStatusCode.Created, tokenPairDto.data)
				}

				is AppResult.Failure -> {
					throw BusinessException(HttpStatusCode.BadRequest, tokenPairDto.message)
				}
			}
		}
	}

	route("/auth/login") {
		post {
			val loginDto = call.receive<LoginDto>()
			val tokenPairDto = authService.login(loginDto)

			when (tokenPairDto) {
				is AppResult.Success -> {
					call.respond(HttpStatusCode.OK, tokenPairDto.data)
				}

				is AppResult.Failure -> {
					throw BusinessException(HttpStatusCode.Unauthorized, tokenPairDto.message)
				}
			}
		}
	}

	route("/auth/refresh") {
		post {
			val refreshDto = call.receive<RefreshDto>()
			val tokenPairDto = authService.refresh(refreshDto)

			when (tokenPairDto) {
				is AppResult.Success -> {
					call.respond(HttpStatusCode.OK, tokenPairDto.data)
				}

				is AppResult.Failure -> {
					throw BusinessException(HttpStatusCode.Unauthorized, tokenPairDto.message)
				}
			}
		}
	}

	authenticate("auth-jwt") {
		route("/auth/logout") {
			post {
				val jwt = call.principal<JWTPrincipal>()
				if (jwt != null) {
					val id = UUID.fromString(jwt.subject)
					val logoutResult = authService.logout(id)

					when (logoutResult) {
						is AppResult.Success -> {
							call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
						}

						is AppResult.Failure -> {
							throw BusinessException(HttpStatusCode.InternalServerError, logoutResult.message)
						}
					}
				}
				else {
					throw BusinessException(HttpStatusCode.Unauthorized, "Invalid authentication")
				}
			}
		}
	}
}