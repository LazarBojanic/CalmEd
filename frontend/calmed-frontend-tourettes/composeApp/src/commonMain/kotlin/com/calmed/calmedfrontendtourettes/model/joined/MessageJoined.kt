package com.calmed.calmedfrontendtourettes.model.joined

import kotlinx.serialization.Serializable

@Serializable
data class MessageJoined(
	val id: String,
	val text: String?,
	val createdAt: String?,
	val updatedAt: String?,
)