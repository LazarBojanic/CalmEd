package com.calmed.calmedtics.model.dto.request

import com.calmed.calmedtics.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionDto(
    val paymentType: PaymentType
)
