package com.calmed.calmedtics.billing

interface BillingService {
    suspend fun connect()
    suspend fun loadProduct(productId: String): Boolean
    suspend fun purchase(productId: String)
    suspend fun restore()
    fun close()
}