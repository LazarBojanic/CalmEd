package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.storeentitlement.StoreEntitlement
import com.calmed.calmedbackend.model.raw.storeentitlement.StoreEntitlementEntity
import com.calmed.calmedbackend.model.raw.storeentitlement.StoreEntitlementProvider
import com.calmed.calmedbackend.model.raw.storeentitlement.StoreEntitlementTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IStoreEntitlementRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class StoreEntitlementRepository : IStoreEntitlementRepository {

    override suspend fun findById(id: UUID): StoreEntitlement? {
        return StoreEntitlementEntity.findById(id)?.toRaw()
    }

    override suspend fun findByUserId(userId: UUID): List<StoreEntitlement> {
        return StoreEntitlementEntity
            .find { StoreEntitlementTable.userId eq userId }
            .map { it.toRaw() }
    }

    override suspend fun findByStoreTransactionId(
        store: StoreEntitlementProvider,
        storeTransactionId: String
    ): StoreEntitlement? {
        return StoreEntitlementEntity
            .find {
                (StoreEntitlementTable.store eq store) and
                    (StoreEntitlementTable.storeTransactionId eq storeTransactionId)
            }
            .firstOrNull()
            ?.toRaw()
    }

    override suspend fun create(entitlement: StoreEntitlement): StoreEntitlement? {
        return StoreEntitlementEntity.new(entitlement.id) {
            setFrom(entitlement, MapMode.CREATE)
        }.toRaw()
    }

    override suspend fun update(entitlement: StoreEntitlement): StoreEntitlement? {
        val e = StoreEntitlementEntity.findById(entitlement.id) ?: return null
        e.setFrom(entitlement, MapMode.UPDATE)
        return e.toRaw()
    }

    override suspend fun detachByUserId(userId: UUID): Boolean {
        val entities = StoreEntitlementEntity.find { StoreEntitlementTable.userId eq userId }
        var updated = false
        for (e in entities) {
            if (e.userId != null) {
                e.userId = null
                e.updatedAt = java.time.Instant.now()
                updated = true
            }
        }
        return updated
    }
}
