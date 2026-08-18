package com.calmed.calmedbackend.model.raw.payment

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.*

class PaymentEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<PaymentEntity>(PaymentTable)
	var userId by PaymentTable.userId
	var provider by PaymentTable.provider
	// Google Play
	var googlePurchaseToken by PaymentTable.googlePurchaseToken
	var googleOrderId by PaymentTable.googleOrderId
	// Apple
	var appleTransactionId by PaymentTable.appleTransactionId
	var appleOriginalTransactionId by PaymentTable.appleOriginalTransactionId
	// Stripe
	var stripePaymentIntentId by PaymentTable.stripePaymentIntentId
	var stripeCheckoutSessionId by PaymentTable.stripeCheckoutSessionId
	// PayPal
	var paypalOrderId by PaymentTable.paypalOrderId
	var paypalCaptureId by PaymentTable.paypalCaptureId
	var status by PaymentTable.status
	var refundedAt by PaymentTable.refundedAt
	var createdAt by PaymentTable.createdAt
	var updatedAt by PaymentTable.updatedAt
}