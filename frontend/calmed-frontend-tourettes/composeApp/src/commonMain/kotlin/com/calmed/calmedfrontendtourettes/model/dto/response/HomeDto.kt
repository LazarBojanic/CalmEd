package com.calmed.calmedfrontendtourettes.model.home

import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val greetingName: String? = null,
    val avatarUrl: String? = null,
    val calendar: CalendarMonthDto,
    val upNext: List<ProgramExerciseDto>,
    val currentWeek: Int

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

@Serializable
data class ProgramExerciseDto(
    val id: String,
    val weekNumber: Int,
    val title: String,
    val description: String?,
    val videoURL: String?,
    val thumbnailURL: String?,
    val orderInWeek: Int?
)