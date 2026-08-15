package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class VerifyAppleReceiptDto(
    val transactionId: String,
    val productId: String
)

@Serializable
data class VerifyGoogleReceiptDto(
    val orderId: String = "",
    val productId: String = "",
    val purchaseToken: String = "",
    val purchaseData: String = "",
    val signature: String = "",
)
