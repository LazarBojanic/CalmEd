package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.ProgramConstants
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserExerciseProgressJoined
import com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession
import com.calmed.calmedbackend.model.raw.userexerciseprogress.UserExerciseProgress
import com.calmed.calmedbackend.repository.specification.IUserExerciseProgressRepository
import com.calmed.calmedbackend.service.specification.IUserExerciseProgressService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.time.LocalDate
import java.util.UUID

class UserExerciseProgressService(
	private val repository: IUserExerciseProgressRepository,
	private val userService: IUserService,
) : IUserExerciseProgressService {
	override suspend fun getAll(): AppResult<List<UserExerciseProgressJoined>> {
		return withTransaction {
			val usersById = when (val usersResult = userService.getAll()) {
				is AppResult.Success -> usersResult.data.associateBy { it.id }
				is AppResult.Failure -> return@withTransaction AppResult.Failure(
					usersResult.httpStatusCode, "Failed to retrieve users. ${usersResult.message}"
				)
			}
			val result = mutableListOf<UserExerciseProgressJoined>()
			for (raw in repository.findAll()) {
				val user = usersById[raw.userId]
					?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to retrieve user.")
				result.add(raw.join(user))
			}
			AppResult.Success(result)
		}
	}

	override suspend fun getById(id: UUID): AppResult<UserExerciseProgressJoined> {
		return withTransaction {
			val raw = repository.findById(id) ?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Progress not found.")
			val userRes = userService.getById(raw.userId)
			val user = when (userRes) {
				is AppResult.Success -> userRes.data
				is AppResult.Failure -> return@withTransaction AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
			}
			AppResult.Success(raw.join(user))
		}
	}

	override suspend fun getAllByUserId(userId: UUID): AppResult<List<UserExerciseProgressJoined>> {
		return withTransaction {
			val userRes = userService.getById(userId)
			val user = when (userRes) {
				is AppResult.Success -> userRes.data
				is AppResult.Failure -> return@withTransaction AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
			}
			val result = mutableListOf<UserExerciseProgressJoined>()
			for (raw in repository.findAllByUserId(userId)) {
				result.add(raw.join(user))
			}
			AppResult.Success(result)
		}
	}

	override suspend fun create(progress: UserExerciseProgress): AppResult<UserExerciseProgressJoined> {
		return withResultTransaction {
			val created = repository.create(progress) ?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create progress.")
			val userRes = userService.getById(created.userId)
			val user = when (userRes) {
				is AppResult.Success -> userRes.data
				is AppResult.Failure -> return@withResultTransaction AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
			}
			AppResult.Success(created.join(user))
		}
	}

	override suspend fun initializeUserProgress(userId: UUID, startDate: LocalDate): AppResult<Unit> {
		return withResultTransaction {
			val now = java.time.Instant.now()
			val sessions = listOf(
				ExerciseSession.MORNING,
				ExerciseSession.EVENING
			)
			val existingKeys = repository.findAllByUserId(userId)
				.map { Triple(it.week, it.day, it.exerciseSession) }
				.toHashSet()

			val toCreate = mutableListOf<UserExerciseProgress>()
			for (week in 1..ProgramConstants.TOTAL_WEEKS) {
				for (session in sessions) {
					for (day in 1..ProgramConstants.DAYS_PER_WEEK) {
						if (Triple(week, day, session) !in existingKeys) {
							toCreate.add(
								UserExerciseProgress(
									id = UUID.randomUUID(),
									userId = userId,
									week = week,
									day = day,
									exerciseSession = session,
									completedAt = null,
									createdAt = now,
									updatedAt = now
								)
							)
						}
					}
				}
			}
			if (toCreate.isNotEmpty()) {
				repository.createBatch(toCreate)
			}
			AppResult.Success(Unit)
		}
	}

	override suspend fun update(progress: UserExerciseProgress): AppResult<UserExerciseProgressJoined> {
		return withResultTransaction {
			val updated = repository.update(progress) ?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update progress.")
			val userRes = userService.getById(updated.userId)
			val user = when (userRes) {
				is AppResult.Success -> userRes.data
				is AppResult.Failure -> return@withResultTransaction AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
			}
			AppResult.Success(updated.join(user))
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return withResultTransaction {
			if (repository.delete(id)) AppResult.Success(Unit) else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete progress.")
		}
	}

	override suspend fun syncProgress(userId: UUID, dto: com.calmed.calmedbackend.model.dto.request.UserExerciseProgressUpdateDto): AppResult<Unit> {
		return withResultTransaction {
			val existing = repository.findByCriteria(userId, dto.week, dto.day, dto.session)
			val now = java.time.Instant.now()
			if (existing != null) {
				val updated = existing.copy(
					completedAt = if (dto.completed) (existing.completedAt ?: now) else null,
					updatedAt = now
				)
				repository.update(updated) ?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update progress record.")
			} else {
				val progress = UserExerciseProgress(
					id = UUID.randomUUID(),
					userId = userId,
					week = dto.week,
					day = dto.day,
					exerciseSession = dto.session,
					completedAt = if (dto.completed) now else null,
					createdAt = now,
					updatedAt = now
				)
				repository.create(progress) ?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create progress record.")
			}
			AppResult.Success(Unit)
		}
	}
}
