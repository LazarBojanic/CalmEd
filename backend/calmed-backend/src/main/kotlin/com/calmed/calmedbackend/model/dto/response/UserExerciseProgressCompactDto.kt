package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.util.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class UserExerciseProgressCompactDto(
    val exerciseId: String,
    val session: ExerciseSession,
    val date: String,
    val completed: Boolean = true
)
