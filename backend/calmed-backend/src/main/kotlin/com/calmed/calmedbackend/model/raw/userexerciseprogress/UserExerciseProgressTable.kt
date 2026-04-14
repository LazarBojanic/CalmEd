package com.calmed.calmedbackend.model.raw.userexerciseprogress

import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseTable
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

object UserExerciseProgressTable : UUIDTable("user_exercise_progress") {
	val userId = javaUUID("user_id").references(UserTable.id)
	val week = integer("week")
	val day = integer("day") // 1 through 7
	val exerciseSession = enumeration("exercise_session", ExerciseSession::class)
	val completedAt = timestamp("completed_at").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

	init {
		uniqueIndex(userId, week, day, exerciseSession)
	}
}
