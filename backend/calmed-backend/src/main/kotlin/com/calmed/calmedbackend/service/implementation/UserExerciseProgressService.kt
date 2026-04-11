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
			if (exercise.weekNumber <= 0) continue // Skip non-week exercises (like Intro) or handle differently if needed
			
			val exerciseDateBase = startDate.plusWeeks((exercise.weekNumber - 1).toLong())
			
			// orderInWeek 1 -> MORNING, orderInWeek 2 -> EVENING
			val session = when (exercise.orderInWeek) {
				1 -> com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.MORNING
				2 -> com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.EVENING
				else -> null
			}
			
			if (session != null) {
				// For each day of that week (7 days)
				for (i in 0 until 7) {
					val exerciseDate = exerciseDateBase.plusDays(i.toLong())
					val existing = repository.findByCriteria(userId, exercise.id, session, exerciseDate)
					if (existing == null) {
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
		
		val exercise = programExerciseRepository.findById(exerciseId) ?: return AppResult.Failure(HttpStatusCode.NotFound, "Exercise not found")
		
		// Validate that the session matches the exercise's orderInWeek
		val expectedSession = when (exercise.orderInWeek) {
			1 -> com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.MORNING
			2 -> com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.EVENING
			else -> null
		}
		
		if (expectedSession != null && dto.session != expectedSession) {
			return AppResult.Failure(HttpStatusCode.BadRequest, "Invalid session for this exercise. Expected $expectedSession but got ${dto.session}")
		}
		
		val existing = repository.findByCriteria(userId, exerciseId, dto.session, day)
		val now = java.time.Instant.now()
		if (existing != null) {
			val updated = existing.copy(
				completedAt = if (dto.completed) (existing.completedAt ?: now) else null,
				updatedAt = now
			)
			repository.update(updated) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update progress record.")
		} else {
			// If it doesn't exist (e.g. not pre-initialized), create it
			val progress = UserExerciseProgress(
				id = UUID.randomUUID(),
				userId = userId,
				programExerciseId = exerciseId,
				session = dto.session,
				day = day,
				completedAt = if (dto.completed) now else null,
				createdAt = now,
				updatedAt = now
			)
			repository.create(progress) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create progress record.")
		}
		return AppResult.Success(Unit)
	}
}