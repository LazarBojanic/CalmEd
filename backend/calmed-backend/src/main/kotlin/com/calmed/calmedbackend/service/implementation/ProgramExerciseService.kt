package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.database.withTransaction
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
		return withTransaction {
			AppResult.Success(repository.findAll().map { it.join() })
		}
	}

	override suspend fun getById(id: UUID): AppResult<ProgramExerciseJoined> {
		return withTransaction {
			val found = repository.findById(id)
				?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Program exercise not found.")
			AppResult.Success(found.join())
		}
	}

	override suspend fun getWelcomeVideo(): AppResult<ProgramExerciseJoined> {
		return withTransaction {
			val found = repository.findWelcomeVideo()
			if (found != null) AppResult.Success(found.join())
			else AppResult.Failure(HttpStatusCode.NotFound, "Welcome video not found.")
		}
	}
	override suspend fun getCourseOverviewVideo(): AppResult<ProgramExerciseJoined> {
		return withTransaction {
			val found = repository.findCourseOverviewVideo()
			if (found != null) AppResult.Success(found.join())
			else AppResult.Failure(HttpStatusCode.NotFound, "Course overview video not found.")
		}
	}

	override suspend fun getByWeek(week: Int): AppResult<List<ProgramExerciseJoined>> {
		return withTransaction {
			AppResult.Success(repository.findByWeek(week).map { it.join() })
		}
	}

	override suspend fun getByGroup(group: Int): AppResult<List<ProgramExerciseJoined>> {
		return withTransaction {
			AppResult.Success(repository.findByGroup(group).map { it.join() })
		}
	}

	override suspend fun create(programExercise: ProgramExercise): AppResult<ProgramExerciseJoined> {
		return withResultTransaction {
			val created = repository.create(programExercise)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create program exercise.")
			AppResult.Success(created.join())
		}
	}

	override suspend fun update(programExercise: ProgramExercise): AppResult<ProgramExerciseJoined> {
		return withResultTransaction {
			val updated = repository.update(programExercise)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update program exercise.")
			AppResult.Success(updated.join())
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return withResultTransaction {
			if (repository.delete(id)) AppResult.Success(Unit)
			else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete program exercise.")
		}
	}
}
