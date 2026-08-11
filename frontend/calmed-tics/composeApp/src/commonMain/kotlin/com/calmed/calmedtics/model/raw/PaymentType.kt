package com.calmed.calmedtics.model.raw

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentType {
    CARD,
    GOOGLE,
    APPLE,
    PAYPAL,
    STRIPE,
    SKIP
}
