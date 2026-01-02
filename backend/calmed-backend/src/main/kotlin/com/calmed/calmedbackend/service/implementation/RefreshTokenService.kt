package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import com.calmed.calmedbackend.service.specification.IUserService
import java.time.Instant
import java.util.UUID

class RefreshTokenService(
	private val refreshTokenRepository: IRefreshTokenRepository,
	private val userService: IUserService
) : IRefreshTokenService {
	override suspend fun getAll(): AppResult<List<RefreshTokenJoined>> {
		val result = mutableListOf<RefreshTokenJoined>()

		for (token in refreshTokenRepository.findAll()) {
			val userResult = userService.getById(token.userId)

			if (userResult is AppResult.Success) {
				result.add(token.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}

		return AppResult.Success(result)
	}

	override suspend fun getById(id: UUID): AppResult<RefreshTokenJoined> {
		val token = refreshTokenRepository.findById(id)

		if (token != null) {
			val userResult = userService.getById(token.userId)

			if (userResult is AppResult.Success) {
				return AppResult.Success(token.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}
		else {
			return AppResult.Failure("Refresh token not found.")
		}
	}

	override suspend fun getAllByUserId(userId: UUID): AppResult<List<RefreshTokenJoined>> {
		val result = mutableListOf<RefreshTokenJoined>()
		val tokens = refreshTokenRepository.findAllByUserId(userId)

		for (token in tokens) {
			val userResult = userService.getById(token.userId)

			if (userResult is AppResult.Success) {
				result.add(token.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}

		return AppResult.Success(result)
	}

	override suspend fun revokeById(id: UUID, replacedBy: UUID?): AppResult<Unit> {
		val existing = refreshTokenRepository.findById(id)

		if (existing != null) {
			refreshTokenRepository.update(
				existing.copy(
					revokedAt = Instant.now(),
					replacedBy = replacedBy
				)
			)
			return AppResult.Success(Unit)
		}
		else {
			return AppResult.Failure("Refresh token not found.")
		}
	}

	override suspend fun revokeAllByUserId(userId: UUID, replacedBy: UUID?): AppResult<Unit> {
		val now = Instant.now()
		val tokens = refreshTokenRepository.findAllByUserId(userId)

		for (token in tokens) {
			refreshTokenRepository.update(
				token.copy(
					revokedAt = now,
					replacedBy = replacedBy
				)
			)
		}

		return AppResult.Success(Unit)
	}

	override suspend fun create(refreshToken: RefreshToken): AppResult<RefreshTokenJoined> {
		val created = refreshTokenRepository.create(refreshToken)

		if (created != null) {
			val userResult = userService.getById(created.userId)

			if (userResult is AppResult.Success) {
				return AppResult.Success(created.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}
		else {
			return AppResult.Failure("Failed to create refresh token.")
		}
	}

	override suspend fun update(refreshToken: RefreshToken): AppResult<RefreshTokenJoined> {
		val updated = refreshTokenRepository.update(refreshToken)

		if (updated != null) {
			val userResult = userService.getById(updated.userId)

			if (userResult is AppResult.Success) {
				return AppResult.Success(updated.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}
		else {
			return AppResult.Failure("Failed to update refresh token.")
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		val deleted = refreshTokenRepository.delete(id)

		if (deleted) {
			return AppResult.Success(Unit)
		}
		else {
			return AppResult.Failure("Failed to delete refresh token.")
		}
	}

}
