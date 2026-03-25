package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.ConfirmPaymentIntentDto
import com.calmed.calmedbackend.model.dto.request.CreateCheckoutSessionDto
import com.calmed.calmedbackend.service.specification.IPaymentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.paymentRoutes() {
    val paymentService by inject<IPaymentService>()

    authenticate("auth-jwt") {
        route("/payment") {
            get("/status") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )
                val userId = UUID.fromString(jwt.subject)
                when (val status = paymentService.paymentStatus(userId)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, status.data)
                    is AppResult.Failure -> call.respond(status.httpStatusCode, status.message)
                }
            }

            post("/checkout-session") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )
                val userId = UUID.fromString(jwt.subject)
                val dto = call.receive<CreateCheckoutSessionDto>()
                when (val res = paymentService.createPaymentSheetParams(userId, dto.paymentType)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }

            post("/confirm") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )
                val userId = UUID.fromString(jwt.subject)
                val dto = call.receive<ConfirmPaymentIntentDto>()
                when (val res = paymentService.confirmPaymentIntent(userId, dto.paymentIntentId)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }

            post("/skip-payment") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )
                val userId = UUID.fromString(jwt.subject)
                when (val res = paymentService.skipPayment(userId)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }
        }
    }
}
