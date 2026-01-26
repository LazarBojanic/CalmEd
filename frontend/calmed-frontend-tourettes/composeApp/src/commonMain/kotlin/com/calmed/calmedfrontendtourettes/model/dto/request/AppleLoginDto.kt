package com.calmed.calmedfrontendtourettes.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AppleLoginDto(
    val identityToken: String
)