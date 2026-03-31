package com.calmed.calmedtics.model.dto.response

import com.calmed.calmedtics.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    val isPaid: Boolean,
    val paymentType: PaymentType?,
    val amountCents: Long,
    val currency: String
)
