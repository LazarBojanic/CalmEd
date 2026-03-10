package com.calmed.calmedfrontendtourettes.model.dto.response

import com.calmed.calmedfrontendtourettes.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
	val id: String,
	val email: String,
	val username: String,
	val isEmailVerified: Boolean,
	val isOnboarded: Boolean,
	val isPaid: Boolean,
	val paymentType: PaymentType?,
	val stripeCustomerId: String?,
	val createdAt: String,
	val updatedAt: String
)
