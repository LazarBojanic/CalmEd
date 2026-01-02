package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.toDto
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
import kotlin.getValue

fun Route.authRoutes() {
	val authService by inject<IAuthService>()

	route("/auth/register") {
		post {
			val registerDto = call.receive<RegisterDto>()
			val tokenPairDto = authService.register(registerDto)
			if (tokenPairDto is AppResult.Success) {
				call.respond(HttpStatusCode.OK, tokenPairDto.data)
			}
			else {
				throw BusinessException(HttpStatusCode.Unauthorized, "Failed to register.")
			}
		}
	}

	route("/auth/login") {
		post {
			val loginDto = call.receive<LoginDto>()
			val tokenPairDto = authService.login(loginDto)
			if (tokenPairDto is AppResult.Success) {
				call.respond(HttpStatusCode.OK, tokenPairDto.data)
			}
			else {
				throw BusinessException(HttpStatusCode.Unauthorized, "Failed to login.")
			}
		}
	}

	route("/auth/refresh") {
		post {
			val refreshDto = call.receive<RefreshDto>()
			val tokenPairDto = authService.refresh(refreshDto)
			if (tokenPairDto is AppResult.Success) {
				call.respond(HttpStatusCode.OK, tokenPairDto.data)
			}
			else {
				throw BusinessException(HttpStatusCode.Unauthorized, "Failed to refresh.")
			}
		}
	}

	authenticate("auth-jwt") {
		route("/auth/logout") {
			post {
				val jwt = call.principal<JWTPrincipal>()
				if (jwt != null) {
					val id = UUID.fromString(jwt.subject)
					val tokenPairDto = authService.logout(id)
					if (tokenPairDto is AppResult.Success) {
						call.respond(HttpStatusCode.OK, tokenPairDto.data)
					}
					else {
						throw BusinessException(HttpStatusCode.Unauthorized, "Failed to logout.")
					}
				}
				else {
					throw BusinessException(HttpStatusCode.Unauthorized, "Failed to logout.")
				}
			}
		}
	}
}