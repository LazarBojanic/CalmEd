package com.calmed.calmedfrontendtourettes.model.raw

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentType {
    CARD,
    GOOGLE,
    APPLE
}
