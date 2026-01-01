package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TokenPairDto(
	val access: String,
	val refresh: String
)