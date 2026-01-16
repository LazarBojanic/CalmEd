package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginDto(
    val idToken: String
)