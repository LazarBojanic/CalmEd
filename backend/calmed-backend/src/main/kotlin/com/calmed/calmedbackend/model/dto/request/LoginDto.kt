package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginDto(
	val email: String,
	val password: String
)