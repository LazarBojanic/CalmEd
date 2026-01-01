package com.calmed.calmedbackend.model.raw.refreshtoken

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class RefreshToken(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val replacedBy: UUID?,
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	val tokenHash: String,
	@Serializable(with = InstantSerializer::class)
	val issuedAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val expiresAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val revokedAt: Instant?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
) {
	companion object {
		fun createNew(
			replacedBy: UUID?,
			userId: UUID,
			tokenHash: String,
			issuedAt: Instant,
			expiresAt: Instant,
			revokedAt: Instant?,
			createdAt: Instant? = null,
			updatedAt: Instant? = null
		): RefreshToken {
			val now = Instant.now()
			val cat = createdAt ?: now
			val uat = updatedAt ?: now
			return RefreshToken(
				id = UUID.randomUUID(),
				replacedBy = replacedBy,
				userId = userId,
				tokenHash = tokenHash,
				issuedAt = issuedAt,
				expiresAt = expiresAt,
				revokedAt = revokedAt,
				createdAt = cat,
				updatedAt = uat
			)
		}
	}
	fun isActive(now: Instant = Instant.now()): Boolean {
		return revokedAt == null && expiresAt.isAfter(now)
	}
}
