package com.calmed.calmedbackend.model.raw.payment

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp

object StoreEntitlementTable : UUIDTable(name = "store_entitlement") {
    val store = enumerationByName("store", 16, StoreEntitlementProvider::class)
    val storeTransactionId = varchar("store_transaction_id", 1024)
    val userId = javaUUID("user_id").references(UserTable.id).nullable()
    val productId = varchar("product_id", 255).nullable()
    val obfuscatedAccountId = varchar("obfuscated_account_id", 255).nullable()
    val environment = varchar("environment", 32).nullable()
    val revokedAt = timestamp("revoked_at").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(store, storeTransactionId)
        uniqueIndex(store, userId)
    }
}
