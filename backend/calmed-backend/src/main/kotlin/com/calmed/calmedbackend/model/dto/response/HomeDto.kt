package com.calmed.calmedbackend.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HomeDto(
    val greetingName: String?,
    val avatarUrl: String?,
    val calendar: CalendarMonthDto,
    val upNext: List<UpNextExerciseDto> = emptyList()
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


@Serializable
data class UpNextExerciseDto(
    val id: String,
    val title: String,
    val durationSeconds: Int?,
    val thumbnailUrl: String?,
    val videoUrl: String
)
