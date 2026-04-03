package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SupportMessageResponse(
    val success: Boolean,
    val message: String
)