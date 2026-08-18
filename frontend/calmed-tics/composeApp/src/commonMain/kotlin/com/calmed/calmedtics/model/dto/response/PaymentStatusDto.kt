package com.calmed.calmedtics.model.dto.response

import com.calmed.calmedtics.model.raw.PaymentProvider
import com.calmed.calmedtics.model.raw.payment.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    val hasAccess: Boolean,
    val status: PaymentStatus? = null,
    val provider: PaymentProvider? = null,
    val amount: String,
    val currency: String
)

