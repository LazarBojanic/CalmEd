package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val greetingName: String?,
    val avatarUrl: String?,
    val calendar: CalendarMonthDto,
    val upNext: List<ProgramExerciseDto> = emptyList(),
    val currentWeek: Int
)

@Serializable
data class CalendarDayDto(
    val day: Int,
    val status: String // "UNLOCKED" | "LOCKED" | "INACTIVE"
)

@Serializable
data class CalendarMonthDto(
    val year: Int,
    val month: Int,
    val days: List<CalendarDayDto>
)