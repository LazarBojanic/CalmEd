package com.calmed.calmedbackend.model.raw.exercisegroup

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseGroup(
	val id: Int,
	val name: String,
	val description: String?
)
