package com.calmed.calmedtics.util


import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto

fun ProgramExerciseDto.getTitle(language: String): String {
    return when (language) {
        "es" -> titleEs ?: title
        else -> title
    }
}