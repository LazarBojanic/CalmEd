package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutSessionResponseDto(
    val url: String
)
