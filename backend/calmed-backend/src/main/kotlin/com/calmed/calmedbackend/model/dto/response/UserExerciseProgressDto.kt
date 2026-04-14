package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Serializable
data class UserExerciseProgressDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserDto,
	val week: Int,
	val day: Int,
	val exerciseSession: ExerciseSession,
	@Serializable(with = InstantSerializer::class)
	val completedAt: Instant?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
)
