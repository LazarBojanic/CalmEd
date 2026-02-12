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
	override suspend fun findAll(): List<ProgramExercise> = withTransaction {
		ProgramExerciseEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): ProgramExercise? = withTransaction {
		ProgramExerciseEntity.findById(id)?.toRaw()
	}

	override suspend fun findUpNext(): ProgramExercise? = withTransaction {
		ProgramExerciseEntity.all()
			.orderBy(
				ProgramExerciseTable.weekNumber to SortOrder.ASC,
				ProgramExerciseTable.orderInWeek to SortOrder.ASC
			)
			.limit(1)
			.firstOrNull()
			?.toRaw()
	}

	override suspend fun findUpNextList(limit: Int): List<ProgramExercise> = withTransaction {
		ProgramExerciseEntity.all()
			.orderBy(
				ProgramExerciseTable.weekNumber to SortOrder.ASC,
				ProgramExerciseTable.orderInWeek to SortOrder.ASC
			)
			.limit(limit)
			.map { it.toRaw() }
	}

	override suspend fun findByWeek(week: Int): List<ProgramExercise> = withTransaction {
		ProgramExerciseEntity.find { ProgramExerciseTable.weekNumber eq week }
			.orderBy(ProgramExerciseTable.orderInWeek to SortOrder.ASC)
			.map { it.toRaw() }
	}

	override suspend fun create(programExercise: ProgramExercise): ProgramExercise? = withTransaction {
		val existing = ProgramExerciseEntity.findById(programExercise.id)
		if (existing == null) {
			ProgramExerciseEntity.new(programExercise.id) {
				setFrom(programExercise, MapMode.CREATE)
			}.toRaw()
		} else null
	}

	override suspend fun update(programExercise: ProgramExercise): ProgramExercise? = withTransaction {
		val e = ProgramExerciseEntity.findById(programExercise.id)
		e?.apply { setFrom(programExercise, MapMode.UPDATE) }?.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean = withTransaction {
		ProgramExerciseEntity.findById(id)?.let { it.delete(); true } ?: false
	}
}