package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlement
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlementEntity
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlementProvider
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlementTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IStoreEntitlementRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class StoreEntitlementRepository : IStoreEntitlementRepository {

    override suspend fun findById(id: UUID): StoreEntitlement? {
        return withTransaction {
            StoreEntitlementEntity.findById(id)?.toRaw()
        }
    }

    override suspend fun findByUserId(userId: UUID): List<StoreEntitlement> {
        return withTransaction {
            StoreEntitlementEntity
                .find { StoreEntitlementTable.userId eq userId }
                .map { it.toRaw() }
        }
    }

    override suspend fun findByStoreTransactionId(
        store: StoreEntitlementProvider,
        storeTransactionId: String
    ): StoreEntitlement? {
        return withTransaction {
            StoreEntitlementEntity
                .find {
                    (StoreEntitlementTable.store eq store) and
                        (StoreEntitlementTable.storeTransactionId eq storeTransactionId)
                }
                .firstOrNull()
                ?.toRaw()
        }
    }

    override suspend fun create(entitlement: StoreEntitlement): StoreEntitlement? {
        return withTransaction {
            StoreEntitlementEntity.new(entitlement.id) {
                setFrom(entitlement, MapMode.CREATE)
            }.toRaw()
        }
    }

    override suspend fun update(entitlement: StoreEntitlement): StoreEntitlement? {
        return withTransaction {
            val e = StoreEntitlementEntity.findById(entitlement.id)
            if (e != null) {
                e.setFrom(entitlement, MapMode.UPDATE)
                e.toRaw()
            } else {
                null
            }
        }
    }

    override suspend fun detachByUserId(userId: UUID): Boolean {
        return withTransaction {
            val entities = StoreEntitlementEntity.find { StoreEntitlementTable.userId eq userId }
            var updated = false
            for (e in entities) {
                if (e.userId != null) {
                    e.userId = null
                    e.updatedAt = java.time.Instant.now()
                    updated = true
                }
            }
            updated
        }
    }
}
