package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserInfoTourettesJoined
import com.calmed.calmedbackend.model.raw.userinfo.tourettes.UserInfoTourettes
import com.calmed.calmedbackend.repository.specification.IUserInfoTourettesRepository
import com.calmed.calmedbackend.service.specification.IUserInfoTourettesService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.util.UUID

class UserInfoTourettesService(private val userInfoTourettesRepository: IUserInfoTourettesRepository,
                               private val userService: IUserService
) : IUserInfoTourettesService {
	override suspend fun getAll(): AppResult<List<UserInfoTourettesJoined>> {
		val result = mutableListOf<UserInfoTourettesJoined>()

		for (userInfo in userInfoTourettesRepository.findAll()) {
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

	override suspend fun getById(id: UUID): AppResult<UserInfoTourettesJoined> {
		val userInfo = userInfoTourettesRepository.findById(id) ?: return AppResult.Failure(
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

	override suspend fun getByUserId(userId: UUID): AppResult<UserInfoTourettesJoined> {
		val userResult = userService.getById(userId)
		val user = when (userResult) {
			is AppResult.Success -> userResult.data
			is AppResult.Failure -> return AppResult.Failure(
				userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
			)
		}
		val existing = userInfoTourettesRepository.findByUserId(userId)
		var resolved: UserInfoTourettes? = null
		if (existing != null) {
			resolved = existing
			return AppResult.Success(resolved.join(user))
		}
		else {
			val createdRaw = UserInfoTourettes.createNew(
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

	override suspend fun create(userInfoTourettes: UserInfoTourettes): AppResult<UserInfoTourettesJoined> {
		val created = userInfoTourettesRepository.create(userInfoTourettes) ?: return AppResult.Failure(
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

	override suspend fun update(userInfoTourettes: UserInfoTourettes): AppResult<UserInfoTourettesJoined> {
		val updated = userInfoTourettesRepository.update(userInfoTourettes) ?: return AppResult.Failure(
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
		return if (userInfoTourettesRepository.delete(id)) {
			AppResult.Success(Unit)
		}
		else {
			AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete user info.")
		}
	}

	override suspend fun updateById(id: UUID, dto: UserInfoTourettesUpdateDto): AppResult<UserInfoTourettesJoined> {
		val existing = userInfoTourettesRepository.findById(id) ?: return AppResult.Failure(
			HttpStatusCode.NotFound,
			"User info not found."
		)

		if (existing.userId != dto.userId) {
			return AppResult.Failure(HttpStatusCode.BadRequest, "Invalid userId")
		}
		val updated = userInfoTourettesRepository.updateById(id, dto) ?: return AppResult.Failure(
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