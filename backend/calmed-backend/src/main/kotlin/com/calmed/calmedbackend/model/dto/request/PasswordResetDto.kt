package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetDto(
	val passwordResetToken: String,
	val newPassword: String
)