package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IUserExerciseProgressService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userExerciseProgressRoutes() {
	val service by inject<IUserExerciseProgressService>()

	authenticate("auth-jwt") {
		route("/user-exercise-progress") {
			get("/{id}") {
				val idParam = call.parameters["id"]
				if (idParam == null) {
					call.respond(HttpStatusCode.BadRequest, "Missing id parameter")
					return@get
				}
				val id = UUID.fromString(idParam)
				when (val res = service.getById(id)) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
		}
		route("/user-exercise-progress/user") {
			get("/{userId}") {
				val userIdParam = call.parameters["userId"]
				if (userIdParam == null) {
					call.respond(HttpStatusCode.BadRequest, "Missing userId parameter")
					return@get
				}
				val userId = UUID.fromString(userIdParam)
				when (val res = service.getAllByUserId(userId)) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.map { it.toDto() })
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
		}
	}
}