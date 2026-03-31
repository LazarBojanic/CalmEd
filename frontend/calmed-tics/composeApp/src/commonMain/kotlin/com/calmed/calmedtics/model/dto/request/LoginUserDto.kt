package com.calmed.calmedtics.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginUserDto(
    val email: String,
    val password: String
)