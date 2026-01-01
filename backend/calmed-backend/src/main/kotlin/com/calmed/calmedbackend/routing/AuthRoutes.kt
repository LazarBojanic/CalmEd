package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IAuthService
import com.calmed.calmedbackend.service.specification.IMessageService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Route.authRoutes() {
	val authService by inject<IAuthService>()
	route("/auth/register") {
		get {
			val res = authService
			val resDto = authService.map { it.toDto() }
			call.respond(messageDtos)
		}
	}
}