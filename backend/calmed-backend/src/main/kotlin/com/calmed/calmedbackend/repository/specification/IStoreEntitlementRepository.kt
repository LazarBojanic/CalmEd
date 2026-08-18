package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.payment.StoreEntitlement
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlementProvider
import java.util.UUID

interface IStoreEntitlementRepository {
    suspend fun findById(id: UUID): StoreEntitlement?
    suspend fun findByUserId(userId: UUID): List<StoreEntitlement>
    suspend fun findByStoreTransactionId(
        store: StoreEntitlementProvider,
        storeTransactionId: String
    ): StoreEntitlement?
    suspend fun create(entitlement: StoreEntitlement): StoreEntitlement?
    suspend fun update(entitlement: StoreEntitlement): StoreEntitlement?
    suspend fun detachByUserId(userId: UUID): Boolean
}
