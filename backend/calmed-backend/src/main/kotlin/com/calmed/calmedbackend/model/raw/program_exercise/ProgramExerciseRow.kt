package com.calmed.calmedbackend.repository.program_exercise

import java.util.UUID

data class ProgramExerciseRow(
    val id: UUID,
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String?
)
