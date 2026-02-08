package com.calmed.calmedbackend.model.raw.program_exercise

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object ProgramExerciseTable : UUIDTable("program_exercise") {
    val weekNumber = integer("week_number")
    val title = text("title")
    val description = text("description").nullable()
    val videoUrl = text("video_url")
    val thumbnailUrl = text("thumbnail_url").nullable()
    val orderInWeek = integer("order_in_week")
    val createdAt = timestamp("created_at")
}
