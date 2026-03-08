package com.calmed.calmedbackend.model.dto.request

import com.calmed.calmedbackend.model.raw.user.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionDto(
    val paymentType: PaymentType
)
