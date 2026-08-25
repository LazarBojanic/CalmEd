package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.payment.PaymentProvider
import com.calmed.calmedbackend.model.raw.payment.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    val hasAccess: Boolean,
    val status: PaymentStatus?,
    val provider: PaymentProvider?,
    val amount: String,
    val currency: String
)

