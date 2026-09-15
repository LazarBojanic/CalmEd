package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.database.withTransaction
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
		return withTransaction {
			val usersById = when (val usersResult = userService.getAll()) {
				is AppResult.Success -> usersResult.data.associateBy { it.id }
				is AppResult.Failure -> return@withTransaction AppResult.Failure(
					usersResult.httpStatusCode, "Failed to retrieve users. ${usersResult.message}"
				)
			}

			val result = mutableListOf<UserInfoTicsJoined>()
			for (userInfo in userInfoTicsRepository.findAll()) {
				val user = usersById[userInfo.userId]
					?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to retrieve user.")
				result.add(userInfo.join(user))
			}

			AppResult.Success(result)
		}
	}

	override suspend fun getById(id: UUID): AppResult<UserInfoTicsJoined> {
		return withTransaction {
			val userInfo = userInfoTicsRepository.findById(id) ?: return@withTransaction AppResult.Failure(
				HttpStatusCode.NotFound,
				"User info not found."
			)
			val userResult = userService.getById(userInfo.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(userInfo.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}

	override suspend fun getByUserId(userId: UUID): AppResult<UserInfoTicsJoined> {
		return withResultTransaction {
			val userResult = userService.getById(userId)
			val user = when (userResult) {
				is AppResult.Success -> userResult.data
				is AppResult.Failure -> return@withResultTransaction AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
			val existing = userInfoTicsRepository.findByUserId(userId)
			if (existing != null) {
				return@withResultTransaction AppResult.Success(existing.join(user))
			}
			val createdRaw = UserInfoTics.createNew(
				userId = userId,
				preferredName = null,
				age = null,
				stressLevel = null,
				ticType = null,
				ticFrequency = null,
				ticDuration = null,
				goal = null
			)
			create(createdRaw)
		}
	}

	override suspend fun create(userInfoTics: UserInfoTics): AppResult<UserInfoTicsJoined> {
		return withResultTransaction {
			val created = userInfoTicsRepository.create(userInfoTics) ?: return@withResultTransaction AppResult.Failure(
				HttpStatusCode.NotFound,
				"Failed to create user info."
			)
			val userResult = userService.getById(created.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(created.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}

	override suspend fun update(userInfoTics: UserInfoTics): AppResult<UserInfoTicsJoined> {
		return withResultTransaction {
			val updated = userInfoTicsRepository.update(userInfoTics) ?: return@withResultTransaction AppResult.Failure(
				HttpStatusCode.NotFound,
				"Failed to update user info."
			)
			val userResult = userService.getById(updated.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(updated.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return withResultTransaction {
			if (userInfoTicsRepository.delete(id)) AppResult.Success(Unit)
			else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete user info.")
		}
	}

	override suspend fun updateById(id: UUID, dto: UserInfoTicsUpdateDto): AppResult<UserInfoTicsJoined> {
		return withResultTransaction {
			val existing = userInfoTicsRepository.findById(id) ?: return@withResultTransaction AppResult.Failure(
				HttpStatusCode.NotFound,
				"User info not found."
			)

			if (existing.userId != dto.userId) {
				return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Invalid userId")
			}
			val updated = userInfoTicsRepository.updateById(id, dto) ?: return@withResultTransaction AppResult.Failure(
				HttpStatusCode.NotFound,
				"Failed to update user info."
			)
			val userResult = userService.getById(updated.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(updated.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}
}
