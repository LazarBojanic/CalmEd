package com.calmed.calmedfrontendtourettes.model.joined

import kotlinx.serialization.Serializable

@Serializable
data class UserJoined(
	val id: String,
	val email: String,
	val username: String,
	val isEmailVerified: Boolean,
	val isOnboarded: Boolean,
	val createdAt: String,
	val updatedAt: String
)
