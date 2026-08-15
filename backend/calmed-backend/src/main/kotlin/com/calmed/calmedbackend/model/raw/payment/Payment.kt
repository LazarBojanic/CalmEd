package com.calmed.calmedbackend.model.raw.payment

import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Payment(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	val paymentType: PaymentType,
	val appleOriginalTransactionId: String?,
	val googleOrderId: String?,
	val successful: Boolean,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
) {
	companion object {
		fun createNew(
			userId: UUID,
			paymentType: PaymentType,
			appleOriginalTransactionId: String? = null,
			googleOrderId: String? = null,
			successful: Boolean = false,
			createdAt: Instant? = null,
			updatedAt: Instant? = null
		): Payment {
			val now = Instant.now()
			return Payment(
				id = UUID.randomUUID(),
				userId = userId,
				paymentType = paymentType,
				appleOriginalTransactionId = appleOriginalTransactionId,
				googleOrderId = googleOrderId,
				successful = successful,
				createdAt = createdAt ?: now,
				updatedAt = updatedAt ?: now
			)
		}
	}
}