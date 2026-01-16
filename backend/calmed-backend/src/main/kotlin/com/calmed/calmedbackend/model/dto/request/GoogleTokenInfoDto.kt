package com.calmed.calmedbackend.model.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleTokenInfoDto(
    val email: String? = null,
    @SerialName("email_verified") val emailVerified: String? = null,
    val sub: String? = null,
    val aud: String? = null
)