package com.calmed.calmedbackend.routing

import com.auth0.jwt.exceptions.JWTVerificationException
import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.PasswordResetDto
import com.calmed.calmedbackend.model.dto.request.PasswordResetEmailDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.service.specification.IAuthService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.http.content.resolveResource
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.authRoutes() {
	val authService by inject<IAuthService>()

	route("/auth/register") {
		post {
			val registerDto = call.receive<RegisterDto>()
			val tokenPairDto = authService.register(registerDto)

			when (tokenPairDto) {
				is AppResult.Success -> {
					call.respond(HttpStatusCode.Created, tokenPairDto.data)
				}

				is AppResult.Failure -> {
					throw BusinessException(tokenPairDto.httpStatusCode, tokenPairDto.message)
				}
			}
		}
	}

	route("/auth/login") {
		post {
			val loginDto = call.receive<LoginDto>()
			val tokenPairDto = authService.login(loginDto)

			when (tokenPairDto) {
				is AppResult.Success -> {
					call.respond(HttpStatusCode.OK, tokenPairDto.data)
				}

				is AppResult.Failure -> {
					throw BusinessException(tokenPairDto.httpStatusCode, tokenPairDto.message)
				}
			}
		}
	}

	route("/auth/refresh") {
		post {
			val refreshDto = call.receive<RefreshDto>()
			val tokenPairDto = authService.refresh(refreshDto)

			when (tokenPairDto) {
				is AppResult.Success -> {
					call.respond(HttpStatusCode.OK, tokenPairDto.data)
				}

				is AppResult.Failure -> {
					throw BusinessException(tokenPairDto.httpStatusCode, tokenPairDto.message)
				}
			}
		}
	}
	route("/auth/verify-email") {
		get {
			val token = call.request.queryParameters["token"]
			if (token != null) {
				val result = authService.verifyEmail(token)

				when (result) {
					is AppResult.Success -> {
						call.respondText(
							text = verificationSuccessPage(),
							contentType = ContentType.Text.Html,
							status = HttpStatusCode.OK
						)
					}

					is AppResult.Failure -> {
						call.respondText(
							text = verificationFailurePage(result.message),
							contentType = ContentType.Text.Html,
							status = result.httpStatusCode
						)
					}
				}
			}
			else {
				throw BusinessException(HttpStatusCode.BadRequest, "Missing token")
			}
		}
	}

	route("/auth/forgot-password") {
		post {
			val dto = call.receive<PasswordResetEmailDto>()
			val result = authService.sendPasswordResetEmail(dto.email)

			when (result) {
				is AppResult.Success -> {
					println("Email sent to ${dto.email}")
					call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset email sent"))
				}

				is AppResult.Failure -> {
					println("Failed to send password reset email to ${dto.email}")
					call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset email sent"))
				}
			}
		}
	}

	route("/auth/reset-password") {
		get {
			val token = call.request.queryParameters["token"]
			if (token != null) {
				try {
					authService.passwordResetVerifier().verify(token)
				}
				catch (e: JWTVerificationException) {
					call.respondText(
						text = verificationFailurePage("Invalid or expired reset token."),
						contentType = ContentType.Text.Html,
						status = HttpStatusCode.Unauthorized
					)
				}
				val resource = call.resolveResource("static/auth/reset-password.html")
				if(resource != null) {
					call.respond(resource)
				}
				else{
					throw BusinessException(HttpStatusCode.InternalServerError, "Resource not found")
				}

			}
			else {
				throw BusinessException(HttpStatusCode.BadRequest, "Missing reset token")
			}
		}
		post {
			val dto = call.receive<PasswordResetDto>()
			val result = authService.resetPassword(dto.passwordResetToken, dto.newPassword)

			when (result) {
				is AppResult.Success -> {
					call.respond(HttpStatusCode.OK, mapOf("message" to "Password has been successfully reset"))
				}

				is AppResult.Failure -> {
					throw BusinessException(result.httpStatusCode, result.message)
				}
			}
		}
	}

	authenticate("auth-jwt") {
		route("/auth/logout") {
			post {
				val jwt = call.principal<JWTPrincipal>()
				if (jwt != null) {
					val id = UUID.fromString(jwt.subject)
					val logoutResult = authService.logout(id)

					when (logoutResult) {
						is AppResult.Success -> {
							call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
						}

						is AppResult.Failure -> {
							throw BusinessException(logoutResult.httpStatusCode, logoutResult.message)
						}
					}
				}
				else {
					throw BusinessException(HttpStatusCode.Unauthorized, "Invalid authentication")
				}
			}
		}
	}
}

private fun verificationSuccessPage(): String {
	return """
	<!DOCTYPE html>
	<html>
	<head>
		<meta charset="UTF-8">
		<title>Email Verified</title>
		<style>
			body { font-family: Arial, sans-serif; text-align: center; padding: 40px; }
			h1 { color: #4CAF50; }
		</style>
	</head>
	<body>
		<h1>✅ Email Verified</h1>
		<p>Your email address has been successfully verified.</p>
		<p>You can now return to the app and log in.</p>
	</body>
	</html>
	""".trimIndent()
}

private fun verificationFailurePage(message: String): String {
	return """
	<!DOCTYPE html>
	<html>
	<head>
		<meta charset="UTF-8">
		<title>Verification Failed</title>
		<style>
			body { font-family: Arial, sans-serif; text-align: center; padding: 40px; }
			h1 { color: #F44336; }
		</style>
	</head>
	<body>
		<h1>❌ Verification Failed</h1>
		<p>$message</p>
	</body>
	</html>
	""".trimIndent()
}

private fun passwordResetPage(token: String): String {
	return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>Reset Your Password</title>
        <style>
            body { font-family: Arial, sans-serif; display: flex; justify-content: center; padding-top: 50px; background-color: #f4f4f9; }
            .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); width: 300px; }
            h2 { color: #333; text-align: center; }
            input { width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
            button { width: 100%; padding: 10px; background-color: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; }
            button:hover { background-color: #45a049; }
            #message { margin-top: 15px; text-align: center; font-size: 14px; }
        </style>
    </head>
    <body>
        <div class="container">
            <h2>Reset Password</h2>
            <input type="password" id="newPassword" placeholder="Enter new password" required>
            <input type="password" id="confirmPassword" placeholder="Confirm new password" required>
            <button onclick="submitReset()">Update Password</button>
            <div id="message"></div>
        </div>

        <script>
            async function submitReset() {
                const password = document.getElementById('newPassword').value;
                const confirm = document.getElementById('confirmPassword').value;
                const messageDiv = document.getElementById('message');

                if (password !== confirm) {
                    messageDiv.style.color = 'red';
                    messageDiv.innerText = 'Passwords do not match';
                    return;
                }

                try {
                    const response = await fetch('/auth/reset-password', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            passwordResetToken: '$token',
                            newPassword: password
                        })
                    });

                    const result = await response.json();
                    if (response.ok) {
                        messageDiv.style.color = 'green';
                        messageDiv.innerText = '✅ ' + result.message;
                        // Optional: hide form after success
                        document.querySelector('button').disabled = true;
                    } else {
                        messageDiv.style.color = 'red';
                        messageDiv.innerText = '❌ ' + (result.message || 'Error resetting password');
                    }
                } catch (err) {
                    messageDiv.style.color = 'red';
                    messageDiv.innerText = '❌ Network error';
                }
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}