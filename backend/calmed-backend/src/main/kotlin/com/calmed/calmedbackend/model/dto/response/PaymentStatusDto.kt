package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.payment.PaymentProvider
import com.calmed.calmedbackend.model.raw.payment.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    /** Derived: true if the user currently has entitlement (a successful, non-revoked, non-refunded payment via any provider). */
    val hasAccess: Boolean,
    /** The payment status of the user's most recent payment, if any. */
    val status: PaymentStatus?,
    /** The provider of the active entitlement (APPLE, GOOGLE, STRIPE, PAYPAL, SKIP). Null if no access. */
    val provider: PaymentProvider?,
    val amount: String,
    val currency: String
)

