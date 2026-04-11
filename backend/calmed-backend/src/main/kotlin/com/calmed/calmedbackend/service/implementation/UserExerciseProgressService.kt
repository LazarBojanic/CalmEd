package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserExerciseProgressJoined
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import com.calmed.calmedbackend.repository.specification.IProgramExerciseRepository
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import com.calmed.calmedbackend.service.specification.IProgramExerciseService
import com.calmed.calmedbackend.service.specification.IUserExerciseProgressService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.time.LocalDate
import java.util.UUID

class UserExerciseProgressService(
	private val repository: IUserExerciseProgressRepository,
	private val userService: IUserService,
	private val programExerciseService: IProgramExerciseService,
	private val programExerciseRepository: com.calmed.calmedbackend.repository.specification.IProgramExerciseRepository
) : IUserExerciseProgressService {
	override suspend fun getAll(): AppResult<List<UserExerciseProgressJoined>> {
		val result = mutableListOf<UserExerciseProgressJoined>()
		for (raw in repository.findAll()) {
			val userRes = userService.getById(raw.userId)
			val exRes = programExerciseService.getById(raw.programExerciseId)
			val user = when (userRes) {
				is AppResult.Success -> userRes.data
				is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
			}
			val ex = when (exRes) {
				is AppResult.Success -> exRes.data
				is AppResult.Failure -> return AppResult.Failure(exRes.httpStatusCode, "Failed to retrieve program exercise. ${exRes.message}")
			}
			result.add(raw.join(user, ex))
		}
		return AppResult.Success(result)
	}

	override suspend fun getById(id: UUID): AppResult<UserExerciseProgressJoined> {
		val raw = repository.findById(id) ?: return AppResult.Failure(HttpStatusCode.NotFound, "Progress not found.")
		val userRes = userService.getById(raw.userId)
		val exRes = programExerciseService.getById(raw.programExerciseId)
		val user = when (userRes) {
			is AppResult.Success -> userRes.data
			is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
		}
		val ex = when (exRes) {
			is AppResult.Success -> exRes.data
			is AppResult.Failure -> return AppResult.Failure(exRes.httpStatusCode, "Failed to retrieve program exercise. ${exRes.message}")
		}
		return AppResult.Success(raw.join(user, ex))
	}

	override suspend fun getAllByUserId(userId: UUID): AppResult<List<UserExerciseProgressJoined>> {
		val userRes = userService.getById(userId)
		val user = when (userRes) {
			is AppResult.Success -> userRes.data
			is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
		}
		val list = repository.findAllByUserId(userId)
		val result = mutableListOf<UserExerciseProgressJoined>()
		for (raw in list) {
			val exRes = programExerciseService.getById(raw.programExerciseId)
			val ex = when (exRes) {
				is AppResult.Success -> exRes.data
				is AppResult.Failure -> return AppResult.Failure(exRes.httpStatusCode, "Failed to retrieve program exercise. ${exRes.message}")
			}
			result.add(raw.join(user, ex))
		}
		return AppResult.Success(result)
	}

	override suspend fun create(progress: UserExerciseProgress): AppResult<UserExerciseProgressJoined> {
		val created = repository.create(progress) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create progress.")
		val userRes = userService.getById(created.userId)
		val exRes = programExerciseService.getById(created.programExerciseId)
		val user = when (userRes) {
			is AppResult.Success -> userRes.data
			is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
		}
		val ex = when (exRes) {
			is AppResult.Success -> exRes.data
			is AppResult.Failure -> return AppResult.Failure(exRes.httpStatusCode, "Failed to retrieve program exercise. ${exRes.message}")
		}
		return AppResult.Success(created.join(user, ex))
	}

	override suspend fun initializeUserProgress(userId: UUID, startDate: LocalDate): AppResult<Unit> {
		val allExercises = programExerciseRepository.findAll()
		val now = java.time.Instant.now()
		
		for (exercise in allExercises) {
			// Calculate the date for this exercise based on weekNumber
			// If weekNumber is 1, it's the first week. We assume exercises are meant to be done in that week.
			// For simplicity, we can set the day to (weekNumber - 1) * 7 days after startDate
			val exerciseDate = startDate.plusWeeks((exercise.weekNumber - 1).toLong())
			
			for (session in listOf(com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.MORNING, com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.EVENING)) {
				val progress = UserExerciseProgress(
					id = UUID.randomUUID(),
					userId = userId,
					programExerciseId = exercise.id,
					session = session,
					completedAt = null,
					day = exerciseDate,
					createdAt = now,
					updatedAt = now
				)
				repository.create(progress)
			}
		}
		return AppResult.Success(Unit)
	}

	override suspend fun update(progress: UserExerciseProgress): AppResult<UserExerciseProgressJoined> {
		val updated = repository.update(progress) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update progress.")
		val userRes = userService.getById(updated.userId)
		val exRes = programExerciseService.getById(updated.programExerciseId)
		val user = when (userRes) {
			is AppResult.Success -> userRes.data
			is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
		}
		val ex = when (exRes) {
			is AppResult.Success -> exRes.data
			is AppResult.Failure -> return AppResult.Failure(exRes.httpStatusCode, "Failed to retrieve program exercise. ${exRes.message}")
		}
		return AppResult.Success(updated.join(user, ex))
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return if (repository.delete(id)) AppResult.Success(Unit) else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete progress.")
	}

	override suspend fun syncProgress(userId: UUID, dto: com.calmed.calmedbackend.model.dto.request.UserExerciseProgressUpdateDto): AppResult<Unit> {
		val exerciseId = try { UUID.fromString(dto.exerciseId) } catch (e: Exception) { return AppResult.Failure(HttpStatusCode.BadRequest, "Invalid exerciseId format") }
		val day = try { java.time.LocalDate.parse(dto.date) } catch (e: Exception) { return AppResult.Failure(HttpStatusCode.BadRequest, "Invalid date format") }
		
		val existing = repository.findByCriteria(userId, exerciseId, dto.session, day)
		if (existing != null) {
			val now = java.time.Instant.now()
			val updated = existing.copy(
				completedAt = if (dto.completed) now else null,
				updatedAt = now
			)
			repository.update(updated) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update progress record.")
		} else {
			// If it doesn't exist (which shouldn't happen if initialized properly), create it if completed
			if (dto.completed) {
				val now = java.time.Instant.now()
				val progress = UserExerciseProgress(
					id = UUID.randomUUID(),
					userId = userId,
					programExerciseId = exerciseId,
					session = dto.session,
					day = day,
					completedAt = now,
					createdAt = now,
					updatedAt = now
				)
				repository.create(progress) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create progress record.")
			}
		}
		return AppResult.Success(Unit)
	}
}