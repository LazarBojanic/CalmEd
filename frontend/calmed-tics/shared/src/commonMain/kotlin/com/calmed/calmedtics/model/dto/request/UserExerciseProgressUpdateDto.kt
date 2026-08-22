package com.calmed.calmedtics.model.dto.request

import com.calmed.calmedtics.model.raw.ExerciseSession
import kotlinx.serialization.Serializable

@Serializable
data class UserExerciseProgressUpdateDto(
    val week: Int,
    val day: Int,
    val session: ExerciseSession,
    val completed: Boolean
)
