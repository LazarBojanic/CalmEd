package com.calmed.calmedtics.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okio.ByteString.Companion.decodeBase64

fun jwtDecode(token: String): JsonObject {
	val payload = token.split(".")[1]
	val decoded = payload.decodeBase64()!!.utf8()
	return Json.parseToJsonElement(decoded).jsonObject
}

fun isValidEmail(email: String): Boolean{
	val emailRegex = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
	return emailRegex.matches(email)
}

enum class PasswordValidationError {
	TOO_SHORT, MISSING_UPPERCASE, MISSING_LOWERCASE, MISSING_DIGIT, MISMATCH
}

fun validatePassword(password: String, confirmPassword: String): PasswordValidationError? =
	when {
		password.length < 8 -> PasswordValidationError.TOO_SHORT
		password.none { it.isUpperCase() } -> PasswordValidationError.MISSING_UPPERCASE
		password.none { it.isLowerCase() } -> PasswordValidationError.MISSING_LOWERCASE
		password.none { it.isDigit() } -> PasswordValidationError.MISSING_DIGIT
		password != confirmPassword -> PasswordValidationError.MISMATCH
		else -> null
	}