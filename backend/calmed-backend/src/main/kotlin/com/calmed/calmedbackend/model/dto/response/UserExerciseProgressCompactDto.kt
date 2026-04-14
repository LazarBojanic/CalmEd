package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.util.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class UserExerciseProgressCompactDto(
    val week: Int,
    val day: Int,
    val session: ExerciseSession,
    val completed: Boolean = true
)
