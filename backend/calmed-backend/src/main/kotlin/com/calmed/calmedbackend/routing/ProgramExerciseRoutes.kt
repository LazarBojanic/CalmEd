package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.config.MuxConfig
import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IPaymentService
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
	val muxConfig by inject<MuxConfig>()
	val paymentService by inject<IPaymentService>()

	authenticate("auth-jwt") {
		route("/program-exercises") {
			get("") {
				val hasAccess = paymentService.hasActiveAccess(call.requireSubjectId())
				when (val res = service.getAll()) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.map { it.toDto(muxConfig, hasAccess) })
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}
			get("/welcome-video") {
				when (val res = service.getWelcomeVideo()) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto(muxConfig, hasAccess = true))
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}
			get("/course-overview-video") {
				when (val res = service.getCourseOverviewVideo()) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto(muxConfig, hasAccess = true))
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}
			get("/{id}") {
				val idParam = call.parameters["id"]
					?: throw BusinessException(HttpStatusCode.BadRequest, "Missing id parameter")
				val id = try {
					UUID.fromString(idParam)
				} catch (e: IllegalArgumentException) {
					throw BusinessException(HttpStatusCode.BadRequest, "Invalid id parameter")
				}
				val hasAccess = paymentService.hasActiveAccess(call.requireSubjectId())
				when (val res = service.getById(id)) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.toDto(muxConfig, hasAccess))
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}

			get("/week/{week}") {
				val weekParam = call.parameters["week"]
					?: throw BusinessException(HttpStatusCode.BadRequest, "Missing week parameter")
				val week = weekParam.toIntOrNull()
					?: throw BusinessException(HttpStatusCode.BadRequest, "Invalid week parameter")
				val hasAccess = paymentService.hasActiveAccess(call.requireSubjectId())
				when (val res = service.getByWeek(week)) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.map { it.toDto(muxConfig, hasAccess) })
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}

			get("/group/{group}") {
				val groupParam = call.parameters["group"]
					?: throw BusinessException(HttpStatusCode.BadRequest, "Missing group parameter")
				val group = groupParam.toIntOrNull()
					?: throw BusinessException(HttpStatusCode.BadRequest, "Invalid group parameter")
				val hasAccess = paymentService.hasActiveAccess(call.requireSubjectId())
				when (val res = service.getByGroup(group)) {
					is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data.map { it.toDto(muxConfig, hasAccess) })
					is AppResult.Failure -> throw BusinessException(res.httpStatusCode, res.message)
				}
			}
		}
	}
}
