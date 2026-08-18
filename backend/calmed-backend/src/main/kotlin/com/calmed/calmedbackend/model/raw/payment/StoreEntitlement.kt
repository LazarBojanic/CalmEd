package com.calmed.calmedbackend.model.raw.payment

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID


@Serializable
data class StoreEntitlement(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val store: StoreEntitlementProvider,
    val storeTransactionId: String,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID?,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant
) {
    companion object {
        fun createNew(
            store: StoreEntitlementProvider,
            storeTransactionId: String,
            userId: UUID?,
            createdAt: Instant? = null,
            updatedAt: Instant? = null
        ): StoreEntitlement {
            val now = Instant.now()
            return StoreEntitlement(
                id = UUID.randomUUID(),
                store = store,
                storeTransactionId = storeTransactionId,
                userId = userId,
                createdAt = createdAt ?: now,
                updatedAt = updatedAt ?: now
            )
        }
    }
}
