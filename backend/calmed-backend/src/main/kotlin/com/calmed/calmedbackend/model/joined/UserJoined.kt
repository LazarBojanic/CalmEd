package com.calmed.calmedbackend.model.joined

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class UserJoined(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val email: String,
	val username: String,
	val isEmailVerified: Boolean,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant,
)