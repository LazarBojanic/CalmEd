package com.calmed.calmedbackend.model.raw.programexercise

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.UUID

class ProgramExerciseEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<ProgramExerciseEntity>(ProgramExerciseTable)
	var weekNumber by ProgramExerciseTable.weekNumber
	var title by ProgramExerciseTable.title
	var titleEs by ProgramExerciseTable.titleEs
	var description by ProgramExerciseTable.description
	var playbackId by ProgramExerciseTable.playbackId
	var thumbnailURL by ProgramExerciseTable.thumbnailURL
	var visibility by ProgramExerciseTable.visibility
	var orderInWeek by ProgramExerciseTable.orderInWeek
	var createdAt by ProgramExerciseTable.createdAt
	var updatedAt by ProgramExerciseTable.updatedAt
}
