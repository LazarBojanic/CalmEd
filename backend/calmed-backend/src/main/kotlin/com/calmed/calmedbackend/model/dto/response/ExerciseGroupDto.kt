package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseGroupDto(
	val id: Int,
	val name: String,
	val description: String?
)
