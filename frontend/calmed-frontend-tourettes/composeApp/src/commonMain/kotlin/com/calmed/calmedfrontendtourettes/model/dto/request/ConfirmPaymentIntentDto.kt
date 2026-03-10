package com.calmed.calmedfrontendtourettes.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmPaymentIntentDto(
    val paymentIntentId: String
)
