package com.calmed.calmedtics.billing

class IosBillingService : BillingService {
    override suspend fun connect() {}
    override suspend fun purchase(productId: String) { error("Not implemented yet") }
    override suspend fun restore() {}
    override fun close() {}
    override suspend fun loadProduct(productId: String): Boolean = false
}