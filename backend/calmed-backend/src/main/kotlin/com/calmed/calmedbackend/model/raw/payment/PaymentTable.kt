package com.calmed.calmedbackend.model.raw.payment

import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp

object PaymentTable : UUIDTable(name = "payment") {
	val userId = javaUUID("user_id").references(UserTable.id)
	val paymentType = enumerationByName("payment_type", 32, PaymentType::class)
	val appleOriginalTransactionId = varchar("apple_original_transaction_id", 500).nullable()
	val googleOrderId = varchar("google_order_id", 500).nullable().uniqueIndex()
	val successful = bool("successful").default(false)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}