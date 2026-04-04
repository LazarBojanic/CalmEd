package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SupportMessageRequest(
    val subject: String,
    val message: String,
    val userEmail: String
)