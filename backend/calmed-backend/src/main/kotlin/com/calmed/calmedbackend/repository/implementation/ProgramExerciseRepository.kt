package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseEntity
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExerciseTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IProgramExerciseRepository
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class ProgramExerciseRepository : IProgramExerciseRepository {
	override suspend fun findAll(): List<ProgramExercise>  {
		return withTransaction {
			ProgramExerciseEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): ProgramExercise? {
		return withTransaction {
			ProgramExerciseEntity.findById(id)?.toRaw()
		}
	}

	override suspend fun findWelcomeVideo(): ProgramExercise? {
		return findById(UUID.fromString("3a420f83-c314-4731-b319-310c94e55752"))
	}
	override suspend fun findCourseOverviewVideo(): ProgramExercise? {
		return findById(UUID.fromString("d84f5be0-c5e1-4445-a102-3e16e1b32355"))
	}

	override suspend fun findByWeek(week: Int): List<ProgramExercise> {
		return withTransaction {
			ProgramExerciseEntity
				.find { ProgramExerciseTable.weekNumber eq week }
				.map { it.toRaw() }
		}
	}

	override suspend fun create(programExercise: ProgramExercise): ProgramExercise? {
		return withTransaction {
			val existing = ProgramExerciseEntity.findById(programExercise.id)
			if (existing == null) {
				return@withTransaction ProgramExerciseEntity.new(programExercise.id) {
					setFrom(programExercise, MapMode.CREATE)
				}.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun update(programExercise: ProgramExercise): ProgramExercise? {
		return withTransaction {
			val e = ProgramExerciseEntity.findById(programExercise.id)
			if (e != null) {
				e.setFrom(programExercise, MapMode.UPDATE)
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}


	override suspend fun delete(id: UUID): Boolean {
		return withTransaction {
			val e = ProgramExerciseEntity.findById(id)
			if (e != null) {
				e.delete()
				return@withTransaction true
			}
			else {
				return@withTransaction false
			}
		}
	}
}
