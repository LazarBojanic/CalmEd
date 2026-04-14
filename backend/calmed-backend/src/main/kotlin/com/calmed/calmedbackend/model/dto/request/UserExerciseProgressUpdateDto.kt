package com.calmed.calmedbackend.model.dto.request

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import kotlinx.serialization.Serializable

@Serializable
data class UserExerciseProgressUpdateDto(
    val week: Int,
    val day: Int,
    val session: ExerciseSession,
    val completed: Boolean
)
