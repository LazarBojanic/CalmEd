package com.calmed.calmedfrontendtourettes.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenDto(
	val access: String?,
	val refresh: String?
)