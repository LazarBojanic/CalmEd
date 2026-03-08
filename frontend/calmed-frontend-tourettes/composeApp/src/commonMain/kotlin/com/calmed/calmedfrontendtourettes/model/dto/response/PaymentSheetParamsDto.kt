package com.calmed.calmedfrontendtourettes.model.dto.response

import com.calmed.calmedfrontendtourettes.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class PaymentSheetParamsDto(
    val paymentIntentId: String,
    val paymentIntentClientSecret: String,
    val customerId: String,
    val customerEphemeralKeySecret: String,
    val publishableKey: String,
    val merchantDisplayName: String,
    val merchantCountryCode: String,
    val appleMerchantId: String?,
    val paymentType: PaymentType,
    val amountCents: Long,
    val currency: String
)
