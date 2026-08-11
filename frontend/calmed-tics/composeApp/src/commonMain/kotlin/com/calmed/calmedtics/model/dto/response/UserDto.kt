package com.calmed.calmedtics.model.dto.response

import com.calmed.calmedtics.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
	val id: String,
	val email: String,
	val username: String,
	val profileImageUrl: String? = null,
	val isEmailVerified: Boolean,
	val isOnboarded: Boolean,
	val isPaid: Boolean,
	val stripeCustomerId: String?,
	val createdAt: String,
	val updatedAt: String
)
