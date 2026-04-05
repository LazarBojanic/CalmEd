package com.calmed.calmedtics.util


import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto

private fun normalizedContentLang(language: String): String {
    val primary = language.trim().substringBefore('-').lowercase()
    return if (primary == "es") "es" else "en"
}

fun ProgramExerciseDto.getTitle(language: String): String {
    return when (normalizedContentLang(language)) {
        "es" -> titleEs?.takeIf { it.isNotBlank() } ?: title
        else -> title
    }
}

fun ProgramExerciseDto.getVideoURL(language: String): String? {
    return when (normalizedContentLang(language)) {
        "es" -> videoURLEs?.takeIf { it.isNotBlank() } ?: videoURL
        else -> videoURL
    }
}