package com.calmed.calmedtics.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenDto(
	val access: String?,
	val refresh: String?
)