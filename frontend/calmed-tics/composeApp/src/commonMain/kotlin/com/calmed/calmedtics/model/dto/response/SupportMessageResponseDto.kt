package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SupportMessageResponseDto(
    val success: Boolean,
    val message: String
)