package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserInfoTourettesJoined
import com.calmed.calmedbackend.model.raw.userinfo.UserInfoTourettes
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
				is AppResult.Success -> {
					result.add(userInfo.join(userResult.data))
				}

				is AppResult.Failure -> {
					return AppResult.Failure(
						userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
					)
				}
			}
		}

		return AppResult.Success(result)
	}

	override suspend fun getById(id: UUID): AppResult<UserInfoTourettesJoined> {
		val userInfo = userInfoTourettesRepository.findById(id)

		if (userInfo != null) {
			val userResult = userService.getById(userInfo.userId)
			when (userResult) {
				is AppResult.Success -> {
					return AppResult.Success(userInfo.join(userResult.data))
				}

				is AppResult.Failure -> {
					return AppResult.Failure(
						userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
					)
				}
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "User info not found.")
		}
	}

	override suspend fun getByUserId(userId: UUID): AppResult<UserInfoTourettesJoined> {
		val userInfo = userInfoTourettesRepository.findByUserId(userId)

		if (userInfo != null) {
			val userResult = userService.getById(userId)
			when (userResult) {
				is AppResult.Success -> {
					return AppResult.Success(userInfo.join(userResult.data))
				}

				is AppResult.Failure -> {
					return AppResult.Failure(
						userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
					)
				}
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "User info not found.")
		}
	}

	override suspend fun create(userInfoTourettes: UserInfoTourettes): AppResult<UserInfoTourettesJoined> {
		val created = userInfoTourettesRepository.create(userInfoTourettes)

		if (created != null) {
			val userResult = userService.getById(created.userId)
			when (userResult) {
				is AppResult.Success -> {
					return AppResult.Success(created.join(userResult.data))
				}

				is AppResult.Failure -> {
					return AppResult.Failure(
						userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
					)
				}
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "Failed to create user info.")
		}
	}

	override suspend fun update(userInfoTourettes: UserInfoTourettes): AppResult<UserInfoTourettesJoined> {
		val updated = userInfoTourettesRepository.update(userInfoTourettes)

		if (updated != null) {
			val userResult = userService.getById(updated.userId)
			when (userResult) {
				is AppResult.Success -> {
					return AppResult.Success(updated.join(userResult.data))
				}

				is AppResult.Failure -> {
					return AppResult.Failure(
						userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
					)
				}
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "Failed to update user info.")
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		val deleted = userInfoTourettesRepository.delete(id)

		if (deleted) {
			return AppResult.Success(Unit)
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete user info.")
		}
	}
}