package com.calmed.calmedtics.model.dto.response

import com.calmed.calmedtics.video.VideoQuality
import kotlinx.serialization.Serializable

@Serializable
data class ProgramExerciseDto(
	val id: String,
	val weekNumber: Int,
	val groupId: Int?,
	val title: String,
	val description: String?,
	val token480: String,
	val token720: String,
	val token1080: String,
	val previewToken: String,
	val thumbnailToken: String,
	val previewThumbnailToken: String,
	val playbackId: String,
	val previewPlaybackId: String,
	val thumbnailURL: String,
	val previewThumbnailURL: String,
	val durationSeconds: Int?,
	val visibility: String,
	val createdAt: String,
	val updatedAt: String,
) {
	fun tokenFor(quality: VideoQuality): String? = when (quality) {
		VideoQuality.R480 -> token480.takeIf { it.isNotBlank() }
		VideoQuality.R720 -> token720.takeIf { it.isNotBlank() }
		VideoQuality.R1080 -> token1080.takeIf { it.isNotBlank() }
	}
}
