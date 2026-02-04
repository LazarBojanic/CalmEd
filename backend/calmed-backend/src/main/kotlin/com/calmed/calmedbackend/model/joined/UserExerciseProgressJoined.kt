package com.calmed.calmedbackend.model.joined

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Serializable
data class UserExerciseProgressJoined(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserJoined,
	val programExercise: ProgramExerciseJoined,
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