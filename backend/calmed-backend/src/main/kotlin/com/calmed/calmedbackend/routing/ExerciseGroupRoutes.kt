package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.service.specification.IExerciseGroupService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.exerciseGroupRoutes() {
	val service by inject<IExerciseGroupService>()

	authenticate("auth-jwt") {
		route("/exercise-groups") {
			get("") {
				when (val res = service.getAll()) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
		}
	}
}
