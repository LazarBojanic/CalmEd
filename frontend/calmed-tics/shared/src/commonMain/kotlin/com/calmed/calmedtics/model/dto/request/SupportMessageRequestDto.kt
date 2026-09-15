package com.calmed.calmedtics.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SupportMessageRequestDto(
    val subject: String,
    val message: String
)