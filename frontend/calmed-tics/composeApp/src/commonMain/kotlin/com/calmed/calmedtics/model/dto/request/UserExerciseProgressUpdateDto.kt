package com.calmed.calmedtics.model.dto.request

import com.calmed.calmedtics.model.raw.ExerciseSession
import kotlinx.serialization.Serializable

@Serializable
data class UserExerciseProgressUpdateDto(
    val exerciseId: String,
    val session: ExerciseSession,
    val date: String, // YYYY-MM-DD
    val completed: Boolean
)
