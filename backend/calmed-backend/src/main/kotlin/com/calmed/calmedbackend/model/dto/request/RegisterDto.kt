package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDto(
	val email: String,
	val username: String,
	val password: String,
	val confirmPassword: String
)