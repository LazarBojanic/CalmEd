package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserExerciseProgressJoined
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
		val result = mutableListOf<UserExerciseProgressJoined>()
		for (raw in repository.findAll()) {
			val userRes = userService.getById(raw.userId)
			val user = when (userRes) {
				is AppResult.Success -> userRes.data
				is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
			}
			result.add(raw.join(user))
		}
		return AppResult.Success(result)
	}

	override suspend fun getById(id: UUID): AppResult<UserExerciseProgressJoined> {
		val raw = repository.findById(id) ?: return AppResult.Failure(HttpStatusCode.NotFound, "Progress not found.")
		val userRes = userService.getById(raw.userId)
		val user = when (userRes) {
			is AppResult.Success -> userRes.data
			is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
		}
		return AppResult.Success(raw.join(user))
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
			result.add(raw.join(user))
		}
		return AppResult.Success(result)
	}

	override suspend fun create(progress: UserExerciseProgress): AppResult<UserExerciseProgressJoined> {
		val created = repository.create(progress) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create progress.")
		val userRes = userService.getById(created.userId)
		val user = when (userRes) {
			is AppResult.Success -> userRes.data
			is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
		}
		return AppResult.Success(created.join(user))
	}

	override suspend fun initializeUserProgress(userId: UUID, startDate: LocalDate): AppResult<Unit> {
		val maxWeek = 8
		val now = java.time.Instant.now()
		
		for (week in 1..maxWeek) {
			for (session in listOf(com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.MORNING, com.calmed.calmedbackend.model.raw.userexerciseprogress.ExerciseSession.EVENING)) {
				for (day in 1..7) {
					val existing = repository.findByCriteria(userId, week, day, session)
					if (existing == null) {
						val progress = UserExerciseProgress(
							id = UUID.randomUUID(),
							userId = userId,
							week = week,
							day = day,
							exerciseSession = session,
							completedAt = null,
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
		val user = when (userRes) {
			is AppResult.Success -> userRes.data
			is AppResult.Failure -> return AppResult.Failure(userRes.httpStatusCode, "Failed to retrieve user. ${userRes.message}")
		}
		return AppResult.Success(updated.join(user))
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return if (repository.delete(id)) AppResult.Success(Unit) else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete progress.")
	}

	override suspend fun syncProgress(userId: UUID, dto: com.calmed.calmedbackend.model.dto.request.UserExerciseProgressUpdateDto): AppResult<Unit> {
		val existing = repository.findByCriteria(userId, dto.week, dto.day, dto.session)
		val now = java.time.Instant.now()
		if (existing != null) {
			val updated = existing.copy(
				completedAt = if (dto.completed) (existing.completedAt ?: now) else null,
				updatedAt = now
			)
			repository.update(updated) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update progress record.")
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
			repository.create(progress) ?: return AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create progress record.")
		}
		return AppResult.Success(Unit)
	}
}