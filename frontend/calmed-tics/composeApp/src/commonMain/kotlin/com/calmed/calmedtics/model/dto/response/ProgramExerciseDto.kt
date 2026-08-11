package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ProgramExerciseDto(
	val id: String,
	val weekNumber: Int,
	val title: String,
	val description: String?,
	val playbackId: String?,
	val videoURL: String?,
	val thumbnailURL: String?,
	val durationSeconds: Int?,
	val visibility: String,
	val orderInWeek: Int?,
	val createdAt: String,
	val updatedAt: String
)
