package com.calmed.calmedbackend.model.raw.programexercise

import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import java.time.Instant
import java.util.UUID

object ProgramExerciseTable : UUIDTable("program_exercise") {
	val weekNumber = integer("week_number")
	val title = text("title")
	val titleEs = text("title_es").nullable()
	val description = text("description").nullable()
	val playbackId = text("playback_id").nullable()
	val playbackIdEs = text("playback_id_es").nullable()
	val thumbnailURL = text("thumbnail_url").nullable()
	val durationSeconds = integer(name = "duration_seconds").nullable()
	val visibility = enumeration("visibility", Visibility::class).default(Visibility.SIGNED)
	val orderInWeek = integer("order_in_week").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
