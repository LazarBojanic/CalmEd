package com.calmed.calmedbackend.model.raw.payment

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
	val userId: UUID?,
	val provider: PaymentProvider,
	// Google Play
	val googlePurchaseToken: String?,
	val googleOrderId: String?,
	// Apple
	val appleTransactionId: String?,
	val appleOriginalTransactionId: String?,
	// Stripe
	val stripePaymentIntentId: String?,
	val stripeCheckoutSessionId: String?,
	// PayPal
	val paypalOrderId: String?,
	val paypalCaptureId: String?,
	val status: PaymentStatus,
	@Serializable(with = InstantSerializer::class)
	val refundedAt: Instant?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
) {
	companion object {
		fun createNew(
			userId: UUID?,
			provider: PaymentProvider,
			googlePurchaseToken: String? = null,
			googleOrderId: String? = null,
			appleTransactionId: String? = null,
			appleOriginalTransactionId: String? = null,
			stripePaymentIntentId: String? = null,
			stripeCheckoutSessionId: String? = null,
			paypalOrderId: String? = null,
			paypalCaptureId: String? = null,
			status: PaymentStatus = PaymentStatus.PENDING,
			refundedAt: Instant? = null,
			createdAt: Instant? = null,
			updatedAt: Instant? = null
		): Payment {
			val now = Instant.now()
			return Payment(
				id = UUID.randomUUID(),
				userId = userId,
				provider = provider,
				googlePurchaseToken = googlePurchaseToken,
				googleOrderId = googleOrderId,
				appleTransactionId = appleTransactionId,
				appleOriginalTransactionId = appleOriginalTransactionId,
				stripePaymentIntentId = stripePaymentIntentId,
				stripeCheckoutSessionId = stripeCheckoutSessionId,
				paypalOrderId = paypalOrderId,
				paypalCaptureId = paypalCaptureId,
				status = status,
				refundedAt = refundedAt,
				createdAt = createdAt ?: now,
				updatedAt = updatedAt ?: now
			)
		}
	}
}