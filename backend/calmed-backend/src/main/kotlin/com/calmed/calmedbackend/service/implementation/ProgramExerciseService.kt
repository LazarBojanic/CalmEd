package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.ProgramExerciseJoined
import com.calmed.calmedbackend.model.raw.programexercise.ProgramExercise
import com.calmed.calmedbackend.repository.specification.IProgramExerciseRepository
import com.calmed.calmedbackend.service.specification.IProgramExerciseService
import io.ktor.http.HttpStatusCode
import java.util.UUID

class ProgramExerciseService(private val repository: IProgramExerciseRepository) : IProgramExerciseService {
	override suspend fun getAll(): AppResult<List<ProgramExerciseJoined>> {
		val list = repository.findAll().map { it.join() }
		return AppResult.Success(list)
	}

	override suspend fun getById(id: UUID): AppResult<ProgramExerciseJoined> {
		val found = repository.findById(id) ?: return AppResult.Failure(HttpStatusCode.NotFound, "Program exercise not found.")
		return AppResult.Success(found.join())
	}

	override suspend fun getUpNext(): AppResult<ProgramExerciseJoined> {
		val found = repository.findUpNext()
		return if (found != null) {
			AppResult.Success(found.join())
		} else {
			AppResult.Failure(HttpStatusCode.NotFound, "No up next program exercise found.")
		}
	}

	override suspend fun getUpNextList(limit: Int): AppResult<List<ProgramExerciseJoined>> {
		val list = repository.findUpNextList(limit).map { it.join() }
		return AppResult.Success(list)
	}

	override suspend fun getByWeek(week: Int): AppResult<List<ProgramExerciseJoined>> {
		val list = repository.findByWeek(week).map { it.join() }
		return AppResult.Success(list)
	}

	override suspend fun create(programExercise: ProgramExercise): AppResult<ProgramExerciseJoined> {
		val created = repository.create(programExercise) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create program exercise.")
		return AppResult.Success(created.join())
	}

	override suspend fun update(programExercise: ProgramExercise): AppResult<ProgramExerciseJoined> {
		val updated = repository.update(programExercise) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update program exercise.")
		return AppResult.Success(updated.join())
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return if (repository.delete(id)) AppResult.Success(Unit) else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete program exercise.")
	}
}