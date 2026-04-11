package com.calmed.calmedbackend.model.raw.userprogram

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import com.calmed.calmedbackend.util.ZoneIdSerializer
import java.time.LocalDate
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@Serializable
data class UserProgram(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	@Serializable(with = LocalDateSerializer::class)
	val startDate: LocalDate,
	@Serializable(with = LocalDateSerializer::class)
	val endDate: LocalDate?,
	@Serializable(with = ZoneIdSerializer::class)
	val timezone: ZoneId?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
) {
	companion object {
		fun createNew(
			userId: UUID,
			startDate: LocalDate,
			endDate: LocalDate? = null,
			timezone: ZoneId? = null,
			createdAt: Instant? = null,
			updatedAt: Instant? = null
		): UserProgram {
			val now = Instant.now()
			return UserProgram(
				id = UUID.randomUUID(),
				userId = userId,
				startDate = startDate,
				endDate = endDate,
				timezone = timezone,
				createdAt = createdAt ?: now,
				updatedAt = updatedAt ?: now
			)
		}
	}
}