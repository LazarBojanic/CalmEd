package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.payment.Payment
import java.util.UUID

interface IPaymentRepository {
	suspend fun findAll(): List<Payment>
	suspend fun findById(id: UUID): Payment?
	suspend fun findByUserId(userId: UUID): List<Payment>
	suspend fun findByGoogleOrderId(googleOrderId: String): Payment?
	suspend fun findByAppleTransactionId(appleTransactionId: String): Payment?
	suspend fun findByStripeCheckoutSessionId(stripeCheckoutSessionId: String): Payment?
	suspend fun findByPayPalOrderId(paypalOrderId: String): Payment?
	suspend fun create(payment: Payment): Payment?
	suspend fun update(payment: Payment): Payment?
	suspend fun deleteByUserId(userId: UUID): Boolean
	suspend fun delete(id: UUID): Boolean
}

