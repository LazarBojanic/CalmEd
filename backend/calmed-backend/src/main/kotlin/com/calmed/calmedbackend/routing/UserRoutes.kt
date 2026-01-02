package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.implementation.UserService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userRoutes() {
	val userService by inject<IUserService>()
	authenticate("auth-jwt") {
		route("/user") {
			get("{id}") {
				val idParam = call.parameters["id"]
				if (idParam != null) {
					val id = UUID.fromString(idParam)
					val res = userService.getById(id)
					if (res is AppResult.Success) {
						call.respond(res.data.toDto())
					}
					else {
						throw BusinessException(HttpStatusCode.NotFound, "Not found")
					}
				}
				else {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				}
			}
		}
	}

}