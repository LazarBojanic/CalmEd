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
import java.util.UUID

class UserExerciseProgressService(
	private val repository: IUserExerciseProgressRepository,
	private val userService: IUserService,
	private val programExerciseService: IProgramExerciseService
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
}