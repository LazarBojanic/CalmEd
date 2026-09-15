package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val greetingName: String?,
    val avatarUrl: String?,
    val calendar: CalendarMonthDto,
   	val currentWeek: Int,
	val programStartDate: String? = null,
    val completions: List<UserExerciseProgressCompactDto> = emptyList()
)

@Serializable
data class CalendarDayDto(
    val day: Int,
    val status: String // "BEFORE_START" | "DONE" | "AVAILABLE" | "LOCKED"
)

@Serializable
data class CalendarMonthDto(
    val year: Int,
    val month: Int,
    val days: List<CalendarDayDto>
)