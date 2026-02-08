package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.service.specification.HomeService
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject
import io.ktor.server.response.respond


fun Route.homeRoutes() {
    val homeService by inject<HomeService>()

    authenticate("auth-jwt") {
        get("/home") {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.subject
            call.respond(homeService.getHome(userId))


        }
    }
}
