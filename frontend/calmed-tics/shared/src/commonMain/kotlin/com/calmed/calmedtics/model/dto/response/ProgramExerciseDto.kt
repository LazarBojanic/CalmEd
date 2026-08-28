package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProgramExerciseDto(
	val id: String = "",
	val weekNumber: Int = 1,
	val title: String = "",
	val description: String? = null,
	val playbackId: String? = null,
	val previewPlaybackId: String? = null,
	val previewVideoURL: String? = null,
	val videoURL: String? = null,
	val thumbnailURL: String? = null,
	val durationSeconds: Int? = null,
	val visibility: String = "PUBLIC",
	val createdAt: String = "",
	val updatedAt: String = ""
)
