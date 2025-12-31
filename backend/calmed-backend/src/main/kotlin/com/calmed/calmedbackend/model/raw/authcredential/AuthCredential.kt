package com.calmed.calmedbackend.model.raw.authcredential

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class AuthCredential(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	val type: AuthCredentialType,
	val passwordHash: String,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant,
) {
	companion object {
		fun createNew(
			userId: UUID,
			type: AuthCredentialType,
			passwordHash: String,
			createdAt: Instant? = null,
			updatedAt: Instant? = null,
		): AuthCredential {
			val now = Instant.now()
			val cat = createdAt ?: now
			val uat = updatedAt ?: now
			return AuthCredential(
				id = UUID.randomUUID(),
				userId = userId,
				type = type,
				passwordHash = passwordHash,
				createdAt = cat,
				updatedAt = uat
			)
		}
	}
}