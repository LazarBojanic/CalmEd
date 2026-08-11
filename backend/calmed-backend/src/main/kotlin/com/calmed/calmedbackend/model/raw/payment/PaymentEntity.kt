package com.calmed.calmedbackend.model.raw.payment

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.*

class PaymentEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<PaymentEntity>(PaymentTable)
	var userId by PaymentTable.userId
	var paymentType by PaymentTable.paymentType
	var appleOriginalTransactionId by PaymentTable.appleOriginalTransactionId
	var googleOrderId by PaymentTable.googleOrderId
	var stripeCustomerId by PaymentTable.stripeCustomerId
	var successful by PaymentTable.successful
	var createdAt by PaymentTable.createdAt
	var updatedAt by PaymentTable.updatedAt
}