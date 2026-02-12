package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.service.specification.IHomeService
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject
import io.ktor.server.response.respond
private const val BUILD_MARKER = "HOME_ROUTE_BUILD_2026_02_10"


fun Route.homeRoutes() {
    val homeService by inject<IHomeService>()

    authenticate("auth-jwt") {
        get(path = "/home") {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.subject

            val today = java.time.LocalDate.now()
            val year = call.request.queryParameters["year"]?.toIntOrNull() ?: today.year
            val month = call.request.queryParameters["month"]?.toIntOrNull() ?: today.monthValue

            call.respond(homeService.getHome(userId, year, month))
        }

        get("/home-test") {
            call.respond(mapOf("marker" to BUILD_MARKER))
        }
    }
}
