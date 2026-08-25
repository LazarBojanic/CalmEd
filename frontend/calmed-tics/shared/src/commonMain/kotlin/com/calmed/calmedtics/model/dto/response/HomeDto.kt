package com.calmed.calmedtics.model.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val greetingName: String? = null,
    val avatarUrl: String? = null,
    val calendar: CalendarMonthDto,
    val currentWeek: Int,
    val programStartDate: String? = null,
    val completions: List<UserExerciseProgressCompactDto> = emptyList()
)

@Serializable
data class UserExerciseProgressCompactDto(
    val week: Int,
    val day: Int,
    val session: com.calmed.calmedtics.model.raw.ExerciseSession,
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