package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IUserInfoTourettesService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userInfoTourettesRoutes() {
	val userInfoTourettesService by inject<IUserInfoTourettesService>()

	authenticate("auth-jwt") {
		route("/user-info-tourettes") {
			get("/{id}") {
				val idParam = call.parameters["id"]
				if (idParam != null) {
					val id = UUID.fromString(idParam)
					val res = userInfoTourettesService.getById(id)
					when (res) {
						is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
						is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
					}
				} else {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				}
			}

			put("/{id}") {
				val idParam = call.parameters["id"]
				if (idParam == null) {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				}

				val id = UUID.fromString(idParam)
				val dto = call.receive<UserInfoTourettesUpdateDto>()
				val res = userInfoTourettesService.updateById(id, dto)

				when (res) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
					is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
				}
			}
		}

		route("/user-info-tourettes/user") {
			get("/{userId}") {
				val userIdParam = call.parameters["userId"]
				if (userIdParam != null) {
					val userId = UUID.fromString(userIdParam)
					val res = userInfoTourettesService.getByUserId(userId)
					when (res) {
						is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto())
						is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
					}
				} else {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing user id parameter")
				}
			}
		}
	}
}