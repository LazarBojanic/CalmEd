package com.calmed.calmedtics.billing

import kotlinx.coroutines.flow.Flow

interface BillingService {
    val purchaseResults: Flow<PurchaseResult>
    suspend fun connect()
    suspend fun loadProduct(productId: String): Boolean
    suspend fun productPrice(productId: String): String?
    suspend fun purchase(productId: String, obfuscatedAccountId: String? = null)
    suspend fun restore()
    suspend fun completePurchase(purchaseToken: String)
    fun close()
}