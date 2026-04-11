package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val greetingName: String? = null,
    val avatarUrl: String? = null,
    val calendar: CalendarMonthDto,
    val upNext: List<ProgramExerciseDto>,
    val currentWeek: Int,
    val programStartDate: String? = null,
    val completions: List<UserExerciseProgressCompactDto> = emptyList()
)

@Serializable
data class UserExerciseProgressCompactDto(
    val exerciseId: String,
    val session: com.calmed.calmedtics.model.raw.ExerciseSession,
    val date: String,
    val completed: Boolean = true
)

@Serializable
data class CalendarMonthDto(
    val year: Int,
    val month: Int,
    val days: List<CalendarDayDto>
)
@Serializable
enum class CalendarDayStatus {
    BEFORE_START,
    AVAILABLE,
    LOCKED,
    DONE
}
@Serializable
data class CalendarDayDto(
    val day: Int,
    val status: CalendarDayStatus
)