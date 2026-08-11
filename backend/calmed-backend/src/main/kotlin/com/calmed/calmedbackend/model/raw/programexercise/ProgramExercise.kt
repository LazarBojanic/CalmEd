package com.calmed.calmedbackend.model.raw.programexercise

import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ProgramExercise(
	@Serializable(with = UUIDSerializer::class)
	val id: UUID,
	val weekNumber: Int,
	val title: String,
	val description: String?,
	val playbackId: String?,
	val thumbnailURL: String?,
	val durationSeconds: Int?= null,
	val visibility: Visibility,
	val orderInWeek: Int?,
	@Serializable(with = InstantSerializer::class)
	val createdAt: Instant,
	@Serializable(with = InstantSerializer::class)
	val updatedAt: Instant
)