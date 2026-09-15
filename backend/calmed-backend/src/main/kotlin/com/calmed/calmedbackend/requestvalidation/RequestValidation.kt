package com.calmed.calmedbackend.requestvalidation

import com.calmed.calmedbackend.model.ProgramConstants
import com.calmed.calmedbackend.model.dto.request.AppleLoginDto
import com.calmed.calmedbackend.model.dto.request.CapturePayPalOrderDto
import com.calmed.calmedbackend.model.dto.request.CreateCheckoutSessionDto
import com.calmed.calmedbackend.model.dto.request.GoogleLoginDto
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.PasswordResetDto
import com.calmed.calmedbackend.model.dto.request.PasswordResetEmailDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.dto.request.SupportMessageRequest
import com.calmed.calmedbackend.model.dto.request.UserExerciseProgressUpdateDto
import com.calmed.calmedbackend.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedbackend.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedbackend.model.dto.request.VerifyGoogleReceiptDto
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import org.apache.commons.validator.routines.EmailValidator
import java.net.URI

private const val MAX_SUBJECT_LENGTH = 150
private const val MAX_MESSAGE_LENGTH = 5000
private const val MAX_NAME_LENGTH = 255
private const val MIN_PASSWORD_LENGTH = 8

private fun validEmail(email: String?): Boolean = EmailValidator.getInstance().isValid(email)

private fun validHttpUrl(url: String): Boolean {
	if (url.isBlank()) return false
	return try {
		val scheme = URI(url).scheme?.lowercase() ?: return false
		scheme == "http" || scheme == "https" || scheme == "calmed"
	} catch (e: Exception) {
		false
	}
}

fun Application.configureRequestValidation() {
	install(RequestValidation) {
		validate<RegisterDto> { dto ->
			when {
				!validEmail(dto.email) -> ValidationResult.Invalid("Invalid email address.")
				dto.username.isBlank() || dto.username.length > MAX_NAME_LENGTH ->
					ValidationResult.Invalid("Username must be between 1 and $MAX_NAME_LENGTH characters.")
				dto.password.length < MIN_PASSWORD_LENGTH ->
					ValidationResult.Invalid("Password must be at least $MIN_PASSWORD_LENGTH characters.")
				dto.password != dto.confirmPassword ->
					ValidationResult.Invalid("Passwords do not match.")
				else -> ValidationResult.Valid
			}
		}

		validate<LoginDto> { dto ->
			when {
				!validEmail(dto.email) -> ValidationResult.Invalid("Invalid email address.")
				dto.password.isBlank() -> ValidationResult.Invalid("Password is required.")
				else -> ValidationResult.Valid
			}
		}

		validate<PasswordResetEmailDto> { dto ->
			if (validEmail(dto.email)) ValidationResult.Valid
			else ValidationResult.Invalid("Invalid email address.")
		}

		validate<PasswordResetDto> { dto ->
			when {
				dto.passwordResetToken.isBlank() -> ValidationResult.Invalid("Password reset token is required.")
				dto.newPassword.length < MIN_PASSWORD_LENGTH ->
					ValidationResult.Invalid("Password must be at least $MIN_PASSWORD_LENGTH characters.")
				else -> ValidationResult.Valid
			}
		}

		validate<RefreshDto> { dto ->
			if (dto.refresh.isBlank()) ValidationResult.Invalid("Refresh token is required.")
			else ValidationResult.Valid
		}

		validate<AppleLoginDto> { dto ->
			if (dto.identityToken.isBlank()) ValidationResult.Invalid("Apple identity token is required.")
			else ValidationResult.Valid
		}

		validate<GoogleLoginDto> { dto ->
			if (dto.idToken.isBlank()) ValidationResult.Invalid("Google ID token is required.")
			else ValidationResult.Valid
		}

		validate<UserInfoTicsUpdateDto> { dto ->
			when {
				dto.preferredName != null && dto.preferredName.length > MAX_NAME_LENGTH ->
					ValidationResult.Invalid("Preferred name is too long.")
				dto.age != null && dto.age !in 0..120 ->
					ValidationResult.Invalid("Age must be between 0 and 120.")
				dto.stressLevel != null && dto.stressLevel !in 0..10 ->
					ValidationResult.Invalid("Stress level must be between 0 and 10.")
				dto.goal != null && dto.goal.length > 1000 ->
					ValidationResult.Invalid("Goal is too long.")
				else -> ValidationResult.Valid
			}
		}

		validate<UserExerciseProgressUpdateDto> { dto ->
			when {
				dto.week !in 1..ProgramConstants.TOTAL_WEEKS ->
					ValidationResult.Invalid("Week must be between 1 and ${ProgramConstants.TOTAL_WEEKS}.")
				dto.day !in 1..ProgramConstants.DAYS_PER_WEEK ->
					ValidationResult.Invalid("Day must be between 1 and ${ProgramConstants.DAYS_PER_WEEK}.")
				else -> ValidationResult.Valid
			}
		}

		validate<SupportMessageRequest> { dto ->
			when {
				dto.subject.isBlank() -> ValidationResult.Invalid("Subject is required.")
				dto.subject.length > MAX_SUBJECT_LENGTH -> ValidationResult.Invalid("Subject is too long.")
				dto.message.isBlank() -> ValidationResult.Invalid("Message is required.")
				dto.message.length > MAX_MESSAGE_LENGTH -> ValidationResult.Invalid("Message is too long.")
				else -> ValidationResult.Valid
			}
		}

		validate<CreateCheckoutSessionDto> { dto ->
			when {
				!validHttpUrl(dto.successUrl) -> ValidationResult.Invalid("Invalid success URL.")
				!validHttpUrl(dto.cancelUrl) -> ValidationResult.Invalid("Invalid cancel URL.")
				else -> ValidationResult.Valid
			}
		}

		validate<VerifyAppleReceiptDto> { dto ->
			when {
				dto.transactionId.isBlank() -> ValidationResult.Invalid("Transaction ID is required.")
				dto.productId.isBlank() -> ValidationResult.Invalid("Product ID is required.")
				else -> ValidationResult.Valid
			}
		}

		validate<VerifyGoogleReceiptDto> { dto ->
			if (dto.purchaseToken.isBlank() && dto.purchaseData.isBlank()) {
				ValidationResult.Invalid("Google purchase token or purchase data is required.")
			} else {
				ValidationResult.Valid
			}
		}

		validate<CapturePayPalOrderDto> { dto ->
			if (dto.orderId.isBlank()) ValidationResult.Invalid("Order ID is required.")
			else ValidationResult.Valid
		}
	}
}
