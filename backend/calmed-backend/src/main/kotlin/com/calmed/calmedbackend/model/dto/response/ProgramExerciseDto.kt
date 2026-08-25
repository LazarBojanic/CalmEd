package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.programexercise.Visibility
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import com.calmed.calmedbackend.util.InstantSerializer
import java.time.Instant
import java.util.UUID

@Serializable
data class ProgramExerciseDto(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val weekNumber: Int,
	val title: String,
	val description: String?,
	val playbackId: String?,
	val videoURL: String?,
	val videoURLEs: String?,
	val thumbnailURL: String?,
	val durationSeconds: Int?,
	val visibility: Visibility,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant

)
