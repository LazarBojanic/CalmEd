package com.calmed.calmedbackend.model.raw.userexerciseprogress

import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseTable
import com.calmed.calmedbackend.util.InstantSerializer
import com.calmed.calmedbackend.util.LocalDateSerializer
import com.calmed.calmedbackend.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class UserExerciseProgressEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<UserExerciseProgressEntity>(UserExerciseProgressTable)
	var userId by UserExerciseProgressTable.userId
	var programExerciseId by UserExerciseProgressTable.programExerciseId
	var session by UserExerciseProgressTable.session
	var completedAt by UserExerciseProgressTable.completedAt
	var day by UserExerciseProgressTable.day
	var createdAt by UserExerciseProgressTable.createdAt
	var updatedAt by UserExerciseProgressTable.updatedAt
}