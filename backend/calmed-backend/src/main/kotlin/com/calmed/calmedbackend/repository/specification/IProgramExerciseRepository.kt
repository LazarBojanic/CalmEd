package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import java.util.UUID

interface IProgramExerciseRepository {
	suspend fun findAll(): List<ProgramExercise>
	suspend fun findById(id: UUID): ProgramExercise?
	suspend fun findUpNext(): ProgramExercise?
	suspend fun findUpNextList(limit: Int): List<ProgramExercise>
	suspend fun findByWeek(week: Int): List<ProgramExercise>
	suspend fun findUpNextByWeek(weekNumber: Int, limit: Int): List<ProgramExercise>
	suspend fun create(programExercise: ProgramExercise): ProgramExercise?
	suspend fun update(programExercise: ProgramExercise): ProgramExercise?
	suspend fun delete(id: UUID): Boolean
}