package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

@Serializable
data class UserExerciseProgressDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val user: UserDto,
	val programExercise: ProgramExerciseDto,
	val session: ExerciseSession?,
	@Serializable(with = LocalDateSerializer::class)
	val day: LocalDate?
)