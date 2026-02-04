package com.calmed.calmedbackend.model.joined

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import com.calmed.calmedbackend.util.ZoneIdSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Serializable
data class UserProgramJoined(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserJoined,
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
)