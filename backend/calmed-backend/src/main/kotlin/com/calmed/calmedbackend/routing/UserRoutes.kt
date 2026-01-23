package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IUserService
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

fun Route.userRoutes() {
	val userService by inject<IUserService>()

	authenticate("auth-jwt") {
		route("/user") {
			get("/{id}") {
				val idParam = call.parameters["id"]
				if (idParam != null) {
					val id = UUID.fromString(idParam)
					val res = userService.getById(id)
					when (res) {
						is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
						is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
					}
				}
				else {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				}
			}

			post("/{id}/onboarded") {
				val idParam = call.parameters["id"]
				if (idParam == null) {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				}
				val id = UUID.fromString(idParam)
				val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
					HttpStatusCode.Unauthorized,
					"Invalid authentication"
				)
				val subjectId = UUID.fromString(jwt.subject)
				if (subjectId != id) {
					throw BusinessException(HttpStatusCode.Forbidden, "Forbidden")
				}
				val dto = call.receive<SetIsOnboardedDto>()
				val res = userService.setIsOnboarded(id, dto.isOnboarded)
				when (res) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
		}
	}
}