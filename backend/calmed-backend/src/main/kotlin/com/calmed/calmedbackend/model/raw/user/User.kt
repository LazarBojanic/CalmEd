package com.calmed.calmedbackend.model.raw.user

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Serializable
data class User(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val email: String,
	val username: String,
	val profileImageUrl: String?,
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
) {
	companion object {
		fun createNew(
			email: String,
			username: String,
			isEmailVerified: Boolean,
			isOnboarded: Boolean,
			isPaid: Boolean = false,
			paymentType: PaymentType? = null,
			stripeCustomerId: String? = null,
			appleOriginalTransactionId: String? = null,
			googleOrderId: String? = null,
			createdAt: Instant? = null,
			updatedAt: Instant? = null,
		): User {
			val now = Instant.now()
			val cat = createdAt ?: now
			val uat = updatedAt ?: now
			return User(
				id = UUID.randomUUID(),
				email = email,
				username = username,
				profileImageUrl = null,
				isEmailVerified = isEmailVerified,
				isOnboarded = isOnboarded,
				isPaid = isPaid,
				paymentType = paymentType,
				stripeCustomerId = stripeCustomerId,
				appleOriginalTransactionId = appleOriginalTransactionId,
				googleOrderId = googleOrderId,
				createdAt = cat,
				updatedAt = uat
			)
		}
	}
}
