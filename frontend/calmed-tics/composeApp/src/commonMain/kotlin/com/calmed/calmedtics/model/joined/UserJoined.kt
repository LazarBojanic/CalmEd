package com.calmed.calmedtics.model.joined

import com.calmed.calmedtics.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class UserJoined(
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
