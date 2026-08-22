package com.calmed.calmedtics.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginDto(
    val idToken: String
)