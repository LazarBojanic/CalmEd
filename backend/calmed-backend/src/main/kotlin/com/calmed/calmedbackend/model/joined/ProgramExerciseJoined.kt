package com.calmed.calmedbackend.model.joined

import com.calmed.calmedbackend.model.raw.programexercise.Visibility
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ProgramExerciseJoined(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val weekNumber: Int,
	val title: String,
	val titleEs: String?,
	val description: String?,
	val playbackId: String?,
	val thumbnailURL: String?,
	val visibility: Visibility,
	val orderInWeek: Int?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
)