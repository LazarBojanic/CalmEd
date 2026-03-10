package com.calmed.calmedfrontendtourettes.model.dto.response

import com.calmed.calmedfrontendtourettes.model.raw.PaymentType
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    val isPaid: Boolean,
    val paymentType: PaymentType?,
    val amountCents: Long,
    val currency: String
)
