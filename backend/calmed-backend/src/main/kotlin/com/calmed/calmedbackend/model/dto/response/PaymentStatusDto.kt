package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.user.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    val isPaid: Boolean,
    val paymentType: PaymentType?,
    val amountCents: Long,
    val currency: String
)
