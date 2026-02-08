package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.joined.ProgramExerciseJoined
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import java.util.UUID

interface IProgramExerciseService {
	suspend fun getAll(): AppResult<List<ProgramExerciseJoined>>
	suspend fun getById(id: UUID): AppResult<ProgramExerciseJoined>
	suspend fun getUpNext(): AppResult<ProgramExerciseJoined>
	suspend fun getUpNextList(limit: Int): AppResult<List<ProgramExerciseJoined>>
	suspend fun getByWeek(week: Int): AppResult<List<ProgramExerciseJoined>>
	suspend fun create(programExercise: ProgramExercise): AppResult<ProgramExerciseJoined>
	suspend fun update(programExercise: ProgramExercise): AppResult<ProgramExerciseJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
}