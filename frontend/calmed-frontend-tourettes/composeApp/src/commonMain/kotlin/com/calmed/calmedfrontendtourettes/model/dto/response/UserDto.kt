package com.calmed.calmedfrontendtourettes.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
	val id: String,
	val email: String,
	val username: String,
	val isEmailVerified: Boolean,
	val isOnboarded: Boolean,
	val createdAt: String,
	val updatedAt: String
)
