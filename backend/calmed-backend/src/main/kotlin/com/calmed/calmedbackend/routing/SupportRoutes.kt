package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.model.dto.request.SupportMessageRequest
import com.calmed.calmedbackend.model.dto.response.SupportMessageResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.supportRoutes() {
    route("/support") {
        post("/message") {
            val request = call.receive<SupportMessageRequest>()

            try {
                val email = org.apache.commons.mail.SimpleEmail()
                email.hostName = "smtp.gmail.com"
                email.setSmtpPort(587)
                email.isStartTLSEnabled = true

                email.setAuthentication(
                    "calmedapplication@gmail.com",
                    "jwqf dqhr wmuv ykor"
                )

                email.setFrom("calmedapplication@gmail.com")
                email.addTo("apps@tagware.com.cy")

                email.subject = "[Support] ${request.subject}"
                email.setMsg(
                    """
                User email: ${request.userEmail}
                Message:
                ${request.message}
                """.trimIndent()
                )
                email.addReplyTo(request.userEmail)

                email.send()

                call.respond(
                    HttpStatusCode.OK,
                    SupportMessageResponse(
                        success = true,
                        message = "Email sent successfully."
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SupportMessageResponse(
                        success = false,
                        message = "Failed to send email."
                    )
                )
            }
        }
    }
}