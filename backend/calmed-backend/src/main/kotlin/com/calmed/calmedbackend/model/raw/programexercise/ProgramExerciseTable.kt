package com.calmed.calmedbackend.model.raw.programexercise

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp

object ProgramExerciseTable : UUIDTable("program_exercise") {
	val weekNumber = integer("week_number")
	val title = varchar("title", 100)
	val description = text("description").nullable()
	val playbackId = text("playback_id").nullable()
	val thumbnailURL = text("thumbnail_url").nullable()
	val visibility = enumeration("visibility", Visibility::class).default(Visibility.SIGNED)
	val orderInWeek = integer("order_in_week").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
