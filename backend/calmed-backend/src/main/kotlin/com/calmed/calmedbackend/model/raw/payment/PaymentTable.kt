package com.calmed.calmedbackend.model.raw.payment

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp

object PaymentTable : UUIDTable(name = "payment") {
	val userId = javaUUID("user_id").references(UserTable.id).nullable()
	val provider = enumerationByName("provider", 32, PaymentProvider::class)
	// Google Play
	val googlePurchaseToken = varchar("google_purchase_token", 1024).nullable()
	val googleOrderId = varchar("google_order_id", 500).nullable().uniqueIndex()
	// Apple
	val appleTransactionId = varchar("apple_transaction_id", 500).nullable()
	val appleOriginalTransactionId = varchar("apple_original_transaction_id", 500).nullable()
	// Stripe
	val stripePaymentIntentId = varchar("stripe_payment_intent_id", 500).nullable()
	val stripeCheckoutSessionId = varchar("stripe_checkout_session_id", 500).nullable()
	// PayPal
	val paypalOrderId = varchar("paypal_order_id", 500).nullable()
	val paypalCaptureId = varchar("paypal_capture_id", 500).nullable()
	val status = enumerationByName("status", 32, PaymentStatus::class).default(PaymentStatus.PENDING)
	val refundedAt = timestamp("refunded_at").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}