package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.config.EmailConfig
import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.SupportMessageRequest
import com.calmed.calmedbackend.model.dto.response.SupportMessageResponse
import com.calmed.calmedbackend.service.implementation.AuthService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

private const val MAX_SUBJECT_LENGTH = 150
private const val MAX_MESSAGE_LENGTH = 5000

fun Route.supportRoutes() {
    val emailConfig by inject<EmailConfig>()
    val authService by inject<AuthService>()
    val userService by inject<IUserService>()
    authenticate("auth-jwt") {
        route("/support") {
            post("/message") {
                val request = call.receive<SupportMessageRequest>()

                val subject = request.subject.take(MAX_SUBJECT_LENGTH)
                val message = request.message.take(MAX_MESSAGE_LENGTH)

                val subjectId = call.requireSubjectId()
                val user = when (val userResult = userService.getById(subjectId)) {
                    is AppResult.Success -> userResult.data
                    is AppResult.Failure -> throw BusinessException(userResult.httpStatusCode, "User not found.")
                }

                authService.sendEmail(
                    from = user.email,
                    to = emailConfig.supportEmail,
                    subject = subject,
                    body = message
                )

                call.respond(
                    HttpStatusCode.OK,
                    SupportMessageResponse(success = true, message = "Email sent successfully.")
                )
            }
        }
    }
}
