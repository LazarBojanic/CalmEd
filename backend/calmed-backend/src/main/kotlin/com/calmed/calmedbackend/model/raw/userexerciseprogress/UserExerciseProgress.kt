package com.calmed.calmedbackend.model.raw.userexerciseprogress

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Serializable
data class UserExerciseProgress (
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	@Serializable(with = UUIDSerializer::class)
	val userId: UUID,
	@Serializable(with = UUIDSerializer::class)
	val programExerciseId: UUID,
	val session: ExerciseSession?,
	@Serializable(with = InstantSerializer::class)
	val completedAt: Instant?,
	@Serializable(with = LocalDateSerializer::class)
	val day: LocalDate?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
)