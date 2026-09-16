package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProgramExerciseDto(
	val id: String,
	val weekNumber: Int,
	val groupId: Int?,
	val title: String,
	val description: String?,
	val token: String,
	val previewToken: String,
	val thumbnailToken: String,
	val previewThumbnailToken: String,
	val playbackId: String,
	val previewPlaybackId: String,
	val url: String,
	val previewURL: String,
	val thumbnailURL: String,
	val previewThumbnailURL: String,
	val durationSeconds: Int?,
	val visibility: String,
	val createdAt: String,
	val updatedAt: String,
)
