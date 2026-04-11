package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.util.UUIDSerializer
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.model.raw.user.PaymentType
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Serializable
data class UserDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val email: String,
	val username: String,
	val isEmailVerified: Boolean,
	val isOnboarded: Boolean,
	val isPaid: Boolean,
	val paymentType: PaymentType?,
	val stripeCustomerId: String?,
	val appleOriginalTransactionId: String?,
	val googleOrderId: String?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
)
