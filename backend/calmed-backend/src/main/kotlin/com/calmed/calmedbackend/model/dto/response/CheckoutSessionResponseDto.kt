package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutSessionResponseDto(
    val sessionId: String,
    val url: String
)
