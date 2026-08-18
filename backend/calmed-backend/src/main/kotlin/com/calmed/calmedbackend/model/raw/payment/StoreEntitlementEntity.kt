package com.calmed.calmedbackend.model.raw.payment

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.UUID

class StoreEntitlementEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<StoreEntitlementEntity>(StoreEntitlementTable)
    var store by StoreEntitlementTable.store
    var storeTransactionId by StoreEntitlementTable.storeTransactionId
    var userId by StoreEntitlementTable.userId
    var createdAt by StoreEntitlementTable.createdAt
    var updatedAt by StoreEntitlementTable.updatedAt
}
