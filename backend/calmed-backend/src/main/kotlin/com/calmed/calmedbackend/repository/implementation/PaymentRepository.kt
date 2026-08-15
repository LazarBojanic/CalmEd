package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
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
		return withTransaction {
			PaymentEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): Payment? {
		return withTransaction {
			PaymentEntity.findById(id)?.toRaw()
		}
	}

	override suspend fun findByUserId(userId: UUID): List<Payment> {
		return withTransaction {
			PaymentEntity.find { PaymentTable.userId eq userId }.map { it.toRaw() }
		}
	}

	override suspend fun findByGoogleOrderId(googleOrderId: String): Payment? {
		return withTransaction {
			PaymentEntity.find { PaymentTable.googleOrderId eq googleOrderId }.firstOrNull()?.toRaw()
		}
	}

	override suspend fun create(payment: Payment): Payment? {
		return withTransaction {
			PaymentEntity.new(payment.id) {
				setFrom(payment, MapMode.CREATE)
			}.toRaw()
		}
	}

	override suspend fun update(payment: Payment): Payment? {
		return withTransaction {
			val e = PaymentEntity.findById(payment.id)
			if (e != null) {
				e.setFrom(payment, MapMode.UPDATE)
				e.toRaw()
			} else {
				null
			}
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return withTransaction {
			val e = PaymentEntity.findById(id)
			if (e != null) {
				e.delete()
				true
			} else {
				false
			}
		}
	}
}
