package com.calmed.calmedfrontendtourettes.model.raw

import kotlinx.serialization.Serializable

@Serializable
data class Message(
	val id: String,
	val text: String?,
	val createdAt: String?,
	val updatedAt: String?,
)