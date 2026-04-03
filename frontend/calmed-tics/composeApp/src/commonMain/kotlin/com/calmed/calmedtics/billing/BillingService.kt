package com.calmed.calmedtics.billing

import kotlinx.coroutines.flow.Flow

interface BillingService {
    val purchaseResults: Flow<PurchaseResult>
    suspend fun connect()
    suspend fun loadProduct(productId: String): Boolean
    suspend fun purchase(productId: String)
    suspend fun restore()
    fun close()
}