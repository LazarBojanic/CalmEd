package com.calmed.calmedbackend.model.dto.request

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import kotlinx.serialization.Serializable

@Serializable
data class UserExerciseProgressUpdateDto(
    val exerciseId: String,
    val session: ExerciseSession,
    val date: String, // YYYY-MM-DD
    val completed: Boolean
)
