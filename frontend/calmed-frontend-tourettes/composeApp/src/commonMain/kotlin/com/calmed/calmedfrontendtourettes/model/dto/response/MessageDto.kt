package com.calmed.calmedfrontendtourettes.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
	val id: String,
	val text: String?,
	val createdAt: String?,
	val updatedAt: String?,
)