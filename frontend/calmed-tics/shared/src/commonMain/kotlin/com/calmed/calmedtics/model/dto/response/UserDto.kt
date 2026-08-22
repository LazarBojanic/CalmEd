package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
	val id: String,
	val email: String,
	val username: String,
	val profileImageUrl: String? = null,
	val isEmailVerified: Boolean,
	val isOnboarded: Boolean,
	val confirmOverEighteen: Boolean,
	val createdAt: String,
	val updatedAt: String
)

