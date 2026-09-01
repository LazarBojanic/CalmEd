package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseGroupDto(
	val id: Int = 0,
	val name: String = "",
	val description: String? = null
)
