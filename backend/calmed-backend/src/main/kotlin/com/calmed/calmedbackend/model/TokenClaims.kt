package com.calmed.calmedbackend.model

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import java.util.UUID
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class TokenClaims(
	val type: TokenType,
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	@Serializable(with = UUIDSerializer::class)
	val sessionId: UUID,
	val email: String,
	val ip: String?,
	val userAgent: String?,
	@Serializable(with = InstantSerializer::class)
	val issuedAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val expiresAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val revokedAt: Instant?
)
enum class TokenType{
	ACCESS, REFRESH
}