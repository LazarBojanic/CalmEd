package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.repository.specification.IUserRepository
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.util.UUID

class UserService(
	private val userRepository: IUserRepository
) : IUserService {

	override suspend fun getAll(): AppResult<List<UserJoined>> {
		return withTransaction {
			AppResult.Success(userRepository.findAll().map { it.join() })
		}
	}

	override suspend fun getById(id: UUID): AppResult<UserJoined> {
		return withTransaction {
			val user = userRepository.findById(id)
			if (user != null) AppResult.Success(user.join())
			else AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun getByEmail(email: String): AppResult<UserJoined> {
		return withTransaction {
			val user = userRepository.findByEmail(email)
			if (user != null) AppResult.Success(user.join())
			else AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun create(user: User): AppResult<UserJoined> {
		return withResultTransaction {
			val created = userRepository.create(user)
			if (created != null) AppResult.Success(created.join())
			else AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create user.")
		}
	}

	override suspend fun update(user: User): AppResult<UserJoined> {
		return withResultTransaction {
			val updated = userRepository.update(user)
			if (updated != null) AppResult.Success(updated.join())
			else AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update user.")
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return withResultTransaction {
			if (userRepository.delete(id)) AppResult.Success(Unit)
			else AppResult.Failure(HttpStatusCode.BadRequest, "Failed to delete user.")
		}
	}

	override suspend fun setIsOnboarded(id: UUID, isOnboarded: Boolean): AppResult<UserJoined> {
		return withResultTransaction {
			val updated = userRepository.setIsOnboarded(id, isOnboarded)
			if (updated != null) AppResult.Success(updated.join())
			else AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun setConfirmOverEighteen(id: UUID, confirmOverEighteen: Boolean): AppResult<UserJoined> {
		return withResultTransaction {
			val updated = userRepository.setConfirmOverEighteen(id, confirmOverEighteen)
			if (updated != null) AppResult.Success(updated.join())
			else AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
		}
	}

	override suspend fun updateProfileImage(
		userId: UUID,
		profileImageUrl: String
	): AppResult<UserJoined> {
		return withResultTransaction {
			val user = userRepository.findById(userId)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.NotFound, "User not found.")

			val updatedUser = user.copy(
				profileImageUrl = profileImageUrl,
				updatedAt = Instant.now()
			)

			val updated = userRepository.update(updatedUser)
			if (updated != null) AppResult.Success(updated.join())
			else AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update profile image.")
		}
	}
}
