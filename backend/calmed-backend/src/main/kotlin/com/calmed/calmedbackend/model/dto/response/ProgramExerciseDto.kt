package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ProgramExerciseDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val weekNumber: Int,
	val title: String,
	val description: String?,
	val videoURL: String?,
	val thumbnailURL: String?,
	val orderInWeek: Int?
)