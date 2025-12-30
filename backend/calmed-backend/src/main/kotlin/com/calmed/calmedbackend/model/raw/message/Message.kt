package com.calmed.calmedbackend.model.raw.message

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Message(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val text: String?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant,
) {
	companion object {
		fun createNew(
			text: String?,
			createdAt: Instant? = null,
			updatedAt: Instant? = null,
		): Message {
			val now = Instant.now()
			val cat = createdAt ?: now
			val uat = updatedAt ?: now
			return Message(
				id = UUID.randomUUID(),
				text = text,
				createdAt = cat,
				updatedAt = uat,
			)
		}
	}
}