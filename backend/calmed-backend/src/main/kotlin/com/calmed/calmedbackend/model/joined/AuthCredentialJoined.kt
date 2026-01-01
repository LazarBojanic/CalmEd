package com.calmed.calmedbackend.model.joined

import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class AuthCredentialJoined(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val userJoined: UserJoined,
	val type: AuthCredentialType,
	val passwordHash: String,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant,
)