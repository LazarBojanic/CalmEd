package com.calmed.calmedbackend.model.raw.userexerciseprogress

import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseTable
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

object UserExerciseProgressTable : UUIDTable("user_exercise_progress") {
	val userId = uuid("user_id").references(UserTable.id)
	val programExerciseId = uuid("program_exercise_id").references(ProgramExerciseTable.id)
	val session = enumeration("session", ExerciseSession::class).nullable()
	val completedAt = timestamp("completed_at").nullable()
	val day = date("day").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
