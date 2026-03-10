package com.calmed.calmedfrontendtourettes.model.dto.request

import com.calmed.calmedfrontendtourettes.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionDto(
    val paymentType: PaymentType
)
