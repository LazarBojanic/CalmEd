package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionDto(
    val successUrl: String,
    val cancelUrl: String
)
