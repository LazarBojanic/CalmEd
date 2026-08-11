package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    val isPaid: Boolean,
    val amount: String,
    val currency: String
)
