package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseEntity
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IProgramExerciseRepository
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class ProgramExerciseRepository : IProgramExerciseRepository {
	override suspend fun findAll(): List<ProgramExercise> {
		return ProgramExerciseEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): ProgramExercise? {
		return ProgramExerciseEntity.findById(id)?.toRaw()
	}

	override suspend fun findWelcomeVideo(): ProgramExercise? {
		return findById(UUID.fromString("3a420f83-c314-4731-b319-310c94e55752"))
	}
	override suspend fun findCourseOverviewVideo(): ProgramExercise? {
		return findById(UUID.fromString("d84f5be0-c5e1-4445-a102-3e16e1b32355"))
	}

	override suspend fun findByWeek(week: Int): List<ProgramExercise> {
		return ProgramExerciseEntity
			.find { ProgramExerciseTable.weekNumber eq week }
			.map { it.toRaw() }
	}

	override suspend fun findByGroup(group: Int): List<ProgramExercise> {
		return ProgramExerciseEntity
			.find { ProgramExerciseTable.groupId eq group }
			.map { it.toRaw() }
			.sortedBy { it.weekNumber }
	}

	override suspend fun create(programExercise: ProgramExercise): ProgramExercise? {
		if (ProgramExerciseEntity.findById(programExercise.id) != null) return null
		return ProgramExerciseEntity.new(programExercise.id) {
			setFrom(programExercise, MapMode.CREATE)
		}.toRaw()
	}

	override suspend fun update(programExercise: ProgramExercise): ProgramExercise? {
		val e = ProgramExerciseEntity.findById(programExercise.id) ?: return null
		e.setFrom(programExercise, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = ProgramExerciseEntity.findById(id) ?: return false
		e.delete()
		return true
	}
}
