package com.calmed.calmedfrontendtourettes.model.home

import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val greetingName: String? = null,
    val avatarUrl: String? = null,
    val calendar: CalendarMonthDto,
    val upNext: List<UpNextExerciseDto>,
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
data class UpNextExerciseDto(
    val id: String,
    val title: String,
    val durationSeconds: Int? = null,
    val thumbnailUrl: String? = null,
    val videoUrl: String
)
