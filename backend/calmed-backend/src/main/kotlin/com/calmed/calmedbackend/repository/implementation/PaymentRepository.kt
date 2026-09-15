package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.payment.Payment
import com.calmed.calmedbackend.model.raw.payment.PaymentEntity
import com.calmed.calmedbackend.model.raw.payment.PaymentTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IPaymentRepository
import org.jetbrains.exposed.v1.core.eq
import java.util.*

class PaymentRepository : IPaymentRepository {
	override suspend fun findAll(): List<Payment> {
		return PaymentEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): Payment? {
		return PaymentEntity.findById(id)?.toRaw()
	}

	override suspend fun findByUserId(userId: UUID): List<Payment> {
		return PaymentEntity.find { PaymentTable.userId eq userId }.map { it.toRaw() }
	}

	override suspend fun findByGoogleOrderId(googleOrderId: String): Payment? {
		return PaymentEntity.find { PaymentTable.googleOrderId eq googleOrderId }.firstOrNull()?.toRaw()
	}

	override suspend fun findByAppleTransactionId(appleTransactionId: String): Payment? {
		return PaymentEntity.find { PaymentTable.appleTransactionId eq appleTransactionId }.firstOrNull()?.toRaw()
	}

	override suspend fun findByAppleOriginalTransactionId(appleOriginalTransactionId: String): Payment? {
		return PaymentEntity.find { PaymentTable.appleOriginalTransactionId eq appleOriginalTransactionId }.firstOrNull()?.toRaw()
	}

	override suspend fun findByStripeCheckoutSessionId(stripeCheckoutSessionId: String): Payment? {
		return PaymentEntity.find { PaymentTable.stripeCheckoutSessionId eq stripeCheckoutSessionId }.firstOrNull()?.toRaw()
	}

	override suspend fun findByPayPalOrderId(paypalOrderId: String): Payment? {
		return PaymentEntity.find { PaymentTable.paypalOrderId eq paypalOrderId }.firstOrNull()?.toRaw()
	}

	override suspend fun create(payment: Payment): Payment? {
		return PaymentEntity.new(payment.id) {
			setFrom(payment, MapMode.CREATE)
		}.toRaw()
	}

	override suspend fun update(payment: Payment): Payment? {
		val e = PaymentEntity.findById(payment.id) ?: return null
		e.setFrom(payment, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = PaymentEntity.findById(id) ?: return false
		e.delete()
		return true
	}

	override suspend fun deleteByUserId(userId: UUID): Boolean {
		val entities = PaymentEntity.find { PaymentTable.userId eq userId }
		var deleted = false
		for (e in entities) {
			e.delete()
			deleted = true
		}
		return deleted
	}
}
