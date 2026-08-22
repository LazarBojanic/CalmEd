package com.calmed.calmedtics.model.raw

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentProvider {
    CARD,
    GOOGLE,
    APPLE,
    PAYPAL,
    STRIPE,
    SKIP
}
