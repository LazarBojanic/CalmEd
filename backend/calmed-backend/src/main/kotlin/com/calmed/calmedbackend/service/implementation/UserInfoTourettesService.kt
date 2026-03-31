package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserInfoTicsJoined
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTics
import com.calmed.calmedbackend.repository.specification.IUserInfoTicsRepository
import com.calmed.calmedbackend.service.specification.IUserInfoTicsService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.util.UUID

class UserInfoTicsService(private val userInfoTicsRepository: IUserInfoTicsRepository,
                               private val userService: IUserService
) : IUserInfoTicsService {
	override suspend fun getAll(): AppResult<List<UserInfoTicsJoined>> {
		val result = mutableListOf<UserInfoTicsJoined>()

		for (userInfo in userInfoTicsRepository.findAll()) {
			val userResult = userService.getById(userInfo.userId)
			when (userResult) {
				is AppResult.Success -> result.add(userInfo.join(userResult.data))
				is AppResult.Failure -> return AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
		}

		return AppResult.Success(result)
	}

	override suspend fun getById(id: UUID): AppResult<UserInfoTicsJoined> {
		val userInfo = userInfoTicsRepository.findById(id) ?: return AppResult.Failure(
			HttpStatusCode.NotFound,
			"User info not found."
		)
		val userResult = userService.getById(userInfo.userId)
		return when (userResult) {
			is AppResult.Success -> AppResult.Success(userInfo.join(userResult.data))
			is AppResult.Failure -> AppResult.Failure(
				userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
			)
		}
	}

	override suspend fun getByUserId(userId: UUID): AppResult<UserInfoTicsJoined> {
		val userResult = userService.getById(userId)
		val user = when (userResult) {
			is AppResult.Success -> userResult.data
			is AppResult.Failure -> return AppResult.Failure(
				userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
			)
		}
		val existing = userInfoTicsRepository.findByUserId(userId)
		var resolved: UserInfoTics? = null
		if (existing != null) {
			resolved = existing
			return AppResult.Success(resolved.join(user))
		}
		else {
			val createdRaw = UserInfoTics.createNew(
				userId = userId,
				preferredName = null,
				age = null,
				stressLevel = null,
				tickType = null,
				tickFrequency = null,
				goal = null,
				followProgress = null
			)
			return create(createdRaw)
		}

	}

	override suspend fun create(userInfoTics: UserInfoTics): AppResult<UserInfoTicsJoined> {
		val created = userInfoTicsRepository.create(userInfoTics) ?: return AppResult.Failure(
			HttpStatusCode.NotFound,
			"Failed to create user info."
		)
		val userResult = userService.getById(created.userId)
		return when (userResult) {
			is AppResult.Success -> AppResult.Success(created.join(userResult.data))
			is AppResult.Failure -> AppResult.Failure(
				userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
			)
		}
	}

	override suspend fun update(userInfoTics: UserInfoTics): AppResult<UserInfoTicsJoined> {
		val updated = userInfoTicsRepository.update(userInfoTics) ?: return AppResult.Failure(
			HttpStatusCode.NotFound,
			"Failed to update user info."
		)
		val userResult = userService.getById(updated.userId)
		return when (userResult) {
			is AppResult.Success -> AppResult.Success(updated.join(userResult.data))
			is AppResult.Failure -> AppResult.Failure(
				userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
			)
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return if (userInfoTicsRepository.delete(id)) {
			AppResult.Success(Unit)
		}
		else {
			AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete user info.")
		}
	}

	override suspend fun updateById(id: UUID, dto: UserInfoTicsUpdateDto): AppResult<UserInfoTicsJoined> {
		val existing = userInfoTicsRepository.findById(id) ?: return AppResult.Failure(
			HttpStatusCode.NotFound,
			"User info not found."
		)

		if (existing.userId != dto.userId) {
			return AppResult.Failure(HttpStatusCode.BadRequest, "Invalid userId")
		}
		val updated = userInfoTicsRepository.updateById(id, dto) ?: return AppResult.Failure(
			HttpStatusCode.NotFound,
			"Failed to update user info."
		)
		val userResult = userService.getById(updated.userId)
		return when (userResult) {
			is AppResult.Success -> AppResult.Success(updated.join(userResult.data))
			is AppResult.Failure -> AppResult.Failure(
				userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
			)
		}
	}
}