package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TokenPairResponse(
	val accessToken: String,
	val refreshToken: String
)