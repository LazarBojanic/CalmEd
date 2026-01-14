package com.calmed.calmedfrontendtourettes.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserDto(
    val email: String,
    val username: String,
    val password: String,
    val confirmPassword: String
)