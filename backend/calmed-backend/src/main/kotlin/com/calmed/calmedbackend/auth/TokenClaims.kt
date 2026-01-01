package com.calmed.calmedbackend.auth

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import java.util.UUID
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class TokenClaims(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val email: String,
	@Serializable(with = InstantSerializer::class)
	val issuedAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val expiresAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val revokedAt: Instant?,
	val type: TokenType
)
