package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.CreateCheckoutSessionDto
import com.calmed.calmedbackend.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedbackend.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedbackend.service.specification.IPaymentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID
import com.calmed.calmedbackend.model.dto.request.CapturePayPalOrderDto

fun Route.paymentRoutes() {
    val paymentService by inject<IPaymentService>()

    route("/payment") {
        post("/webhook") {
            println("[DEBUG_LOG] PaymentRoutes: POST /webhook hit")
            val payload = try {
                call.receiveText()
            } catch (e: Exception) {
                println("[DEBUG_LOG] PaymentRoutes: Failed to receive payload: ${e.message}")
                ""
            }
            println("[DEBUG_LOG] PaymentRoutes: Payload length: ${payload.length}")
            val sigHeader = call.request.headers["Stripe-Signature"] ?: ""
            println("[DEBUG_LOG] PaymentRoutes: Stripe-Signature header present: ${sigHeader.isNotEmpty()}")
            
            when (val res = paymentService.handleStripeWebhook(payload, sigHeader)) {
                is AppResult.Success -> {
                    println("[DEBUG_LOG] PaymentRoutes: Webhook processed successfully")
                    call.respond(HttpStatusCode.OK)
                }
                is AppResult.Failure -> {
                    println("[DEBUG_LOG] PaymentRoutes: Webhook processing FAILURE: ${res.message} (HTTP ${res.httpStatusCode})")
                    call.respond(res.httpStatusCode, res.message)
                }
            }
        }

        authenticate("auth-jwt") {
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
                when (val res = paymentService.createCheckoutSession(userId, dto)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }
            post("/paypal/create-order") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )

                val userId = UUID.fromString(jwt.subject)

                when (val res = paymentService.createPayPalOrder(userId)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }

            post("/paypal/capture-order") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )

                val userId = UUID.fromString(jwt.subject)
                val dto = call.receive<CapturePayPalOrderDto>()

                when (val res = paymentService.capturePayPalOrder(userId, dto)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }

            post("/apple/verify") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )
                val userId = UUID.fromString(jwt.subject)
                val dto = call.receive<VerifyAppleReceiptDto>()
                when (val res = paymentService.verifyApplePurchase(userId, dto)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }

            post("/google/verify") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )
                val userId = UUID.fromString(jwt.subject)
                val dto = call.receive<VerifyGoogleReceiptDto>()
                when (val res = paymentService.verifyGooglePurchase(userId, dto)) {
                    is AppResult.Success -> call.respond(HttpStatusCode.OK, res.data)
                    is AppResult.Failure -> call.respond(res.httpStatusCode, res.message)
                }
            }

            post("/stripe/verify-session") {
                val jwt = call.principal<JWTPrincipal>() ?: throw BusinessException(
                    HttpStatusCode.Unauthorized,
                    "Invalid authentication"
                )
                val userId = UUID.fromString(jwt.subject)
                val sessionId = call.request.queryParameters["sessionId"]
                    ?: throw BusinessException(HttpStatusCode.BadRequest, "Missing sessionId")
                
                when (val res = paymentService.verifyStripeSession(userId, sessionId)) {
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
