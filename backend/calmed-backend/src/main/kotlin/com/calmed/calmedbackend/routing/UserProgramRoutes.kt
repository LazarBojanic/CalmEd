package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IUserProgramService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userProgramRoutes() {
	val service by inject<IUserProgramService>()

	authenticate("auth-jwt") {
		route("/user-programs") {
			get("/{id}") {
				val idParam = call.parameters["id"]
					?: throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				val id = try {
					UUID.fromString(idParam)
				} catch (e: IllegalArgumentException) {
					throw BusinessException(HttpStatusCode.BadRequest, "Invalid id parameter")
				}
				val subject = call.requireSubjectId()
				when (val res = service.getById(id)) {
					is AppResult.Success -> {
						if (res.data.user.id != subject) {
							throw BusinessException(HttpStatusCode.Forbidden, "Forbidden")
						}
						call.respond(HttpStatusCode.OK, res.data.toDto())
					}
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}
		}
		route("/user-programs/user") {
			get("/{userId}") {
				val userIdParam = call.parameters["userId"]
					?: throw BusinessException(HttpStatusCode.BadRequest, "Missing userId parameter")
				val userId = try {
					UUID.fromString(userIdParam)
				} catch (e: IllegalArgumentException) {
					throw BusinessException(HttpStatusCode.BadRequest, "Invalid userId parameter")
				}
				call.requireSelf(userId)
				when (val res = service.getByUserId(userId)) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}
		}
	}
}
