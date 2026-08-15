package com.calmed.calmedtics.billing

import kotlinx.serialization.Serializable

@Serializable
sealed class PurchaseResult {
    @Serializable
    data class Success(
        val paymentType: com.calmed.calmedtics.model.raw.PaymentType,
        val appleTransactionId: String? = null,
        val googleOrderId: String? = null,
        val googlePurchaseToken: String? = null,
        val purchaseData: String? = null,
        val signature: String? = null,
        val productId: String
    ) : PurchaseResult()

    @Serializable
    data class Failure(val message: String) : PurchaseResult()
}
