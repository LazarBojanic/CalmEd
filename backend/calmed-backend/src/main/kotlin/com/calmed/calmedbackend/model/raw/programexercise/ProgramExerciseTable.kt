package com.calmed.calmedbackend.model.raw.programexercise

import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import java.time.Instant
import java.util.UUID

object ProgramExerciseTable : UUIDTable("program_exercise") {
	val weekNumber = integer("week_number")
	val title = varchar("title", 100)
	val description = text("description").nullable()
	val videoURL = text("video_url").nullable()
	val thumbnailURL = text("thumbnail_url").nullable()
	val orderInWeek = integer("order_in_week").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
