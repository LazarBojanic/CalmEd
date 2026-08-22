package com.calmed.calmedtics.model.raw.payment

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentStatus {
    PENDING,
    SUCCESSFUL,
    REFUNDED,
    REVOKED
}
