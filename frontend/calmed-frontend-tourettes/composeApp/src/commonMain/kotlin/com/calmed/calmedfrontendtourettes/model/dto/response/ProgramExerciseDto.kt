package com.calmed.calmedfrontendtourettes.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProgramExerciseDto(
	val id: String,
	val weekNumber: Int,
	val title: String,
	val description: String?,
	val videoURL: String?,
	val thumbnailURL: String?,
	val orderInWeek: Int?
)