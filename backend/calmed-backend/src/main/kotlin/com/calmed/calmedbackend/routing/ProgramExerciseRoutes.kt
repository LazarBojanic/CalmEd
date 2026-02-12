package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IProgramExerciseService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.programExerciseRoutes() {
	val service by inject<IProgramExerciseService>()

	authenticate("auth-jwt") {
		route("/program-exercises") {
			get("") {
				when (val res = service.getAll()) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.map { it.toDto() })
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
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
			get("/up-next") {
				when (val res = service.getUpNext()) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
			get("/week/{week}") {
				val weekParam = call.parameters["week"]
				if (weekParam == null) {
					call.respond(HttpStatusCode.BadRequest, "Missing week parameter")
					return@get
				}
				val week = weekParam.toIntOrNull()
				if (week == null) {
					call.respond(HttpStatusCode.BadRequest, "Invalid week parameter")
					return@get
				}
				when (val res = service.getByWeek(week)) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.map { it.toDto() })
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
		}
	}
}