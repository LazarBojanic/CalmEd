package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IUserInfoTourettesService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID
import kotlin.getValue

fun Route.userInfoTourettesRoutes() {
	val userInfoTourettesService by inject<IUserInfoTourettesService>()
	authenticate("auth-jwt") {
		route("/user-info-tourettes") {
			get("/{id}") {
				val idParam = call.parameters["id"]
				if (idParam != null) {
					val id = UUID.fromString(idParam)
					val res = userInfoTourettesService.getById(id)
					when(res){
						is AppResult.Success -> {
							call.respond(HttpStatusCode.OK, res.data.toDto())
						}
						is AppResult.Failure -> {
							call.respond(res.httpStatusCode, res.message)
						}
					}
				}
				else {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				}
			}
		}
		route("/user-info-tourettes/by-user-id") {
			get("/{userId}") {
				val userIdParam = call.parameters["userId"]
				if (userIdParam != null) {
					val userId = UUID.fromString(userIdParam)
					val res = userInfoTourettesService.getByUserId(userId)
					when(res){
						is AppResult.Success -> {
							call.respond(HttpStatusCode.OK, res.data.toDto())
						}
						is AppResult.Failure -> {
							call.respond(res.httpStatusCode, res.message)
						}
					}
				}
				else {
					throw BusinessException(HttpStatusCode.BadRequest, "Missing user id parameter")
				}
			}
		}
	}

}