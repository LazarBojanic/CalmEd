package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.config.EmailConfig
import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.SupportMessageRequest
import com.calmed.calmedbackend.model.dto.response.SupportMessageResponse
import com.calmed.calmedbackend.service.implementation.AuthService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("SupportRoutes")

private const val MAX_SUBJECT_LENGTH = 150
private const val MAX_MESSAGE_LENGTH = 5000

fun Route.supportRoutes() {
    val emailConfig by inject<EmailConfig>()
    val authService by inject<AuthService>()
    authenticate("auth-jwt") {
        route("/support") {
            post("/message") {
                val request = call.receive<SupportMessageRequest>()

                val subject = request.subject.take(MAX_SUBJECT_LENGTH)
                val message = request.message.take(MAX_MESSAGE_LENGTH)

                if (subject.isBlank() || message.isBlank()) {
                    throw BusinessException(HttpStatusCode.BadRequest, "Subject and message are required")
                }
                val emailValidationResult = authService.validateEmail(request.userEmail)
                when(emailValidationResult) {
                    is AppResult.Success -> {
                        authService.sendEmail(
                            request.userEmail,
                            emailConfig.supportEmail,
                            subject,
                            message
                        )

                        call.respond(
                            HttpStatusCode.OK,
                            SupportMessageResponse(success = true, message = "Email sent successfully.")
                        )
                    }
                    is AppResult.Failure -> {
                        throw BusinessException(HttpStatusCode.BadRequest, "Invalid email address")
                    }
                }
            }
        }
    }
}