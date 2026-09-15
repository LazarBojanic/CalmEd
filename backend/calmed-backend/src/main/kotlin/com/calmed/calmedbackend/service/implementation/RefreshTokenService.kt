package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.util.UUID

class RefreshTokenService(private val refreshTokenRepository: IRefreshTokenRepository,
                          private val userService: IUserService
) : IRefreshTokenService {
	override suspend fun getAll(): AppResult<List<RefreshTokenJoined>> {
		return withTransaction {
			val usersById = when (val usersResult = userService.getAll()) {
				is AppResult.Success -> usersResult.data.associateBy { it.id }
				is AppResult.Failure -> return@withTransaction AppResult.Failure(
					usersResult.httpStatusCode,
					"Failed to retrieve users. ${usersResult.message}"
				)
			}

			val result = mutableListOf<RefreshTokenJoined>()
			for (token in refreshTokenRepository.findAll()) {
				val user = usersById[token.userId]
					?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to retrieve user.")
				result.add(token.join(user))
			}

			AppResult.Success(result)
		}
	}

	override suspend fun getById(id: UUID): AppResult<RefreshTokenJoined> {
		return withTransaction {
			val token = refreshTokenRepository.findById(id)
				?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Refresh token not found.")
			val userResult = userService.getById(token.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(token.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode,
					"Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}

	override suspend fun getAllByUserId(userId: UUID): AppResult<List<RefreshTokenJoined>> {
		return withTransaction {
			val result = mutableListOf<RefreshTokenJoined>()
			for (token in refreshTokenRepository.findAllByUserId(userId)) {
				val userResult = userService.getById(token.userId)
				when (userResult) {
					is AppResult.Success -> result.add(token.join(userResult.data))
					is AppResult.Failure -> return@withTransaction AppResult.Failure(
						userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
					)
				}
			}
			AppResult.Success(result)
		}
	}

	override suspend fun revokeById(id: UUID, replacedBy: UUID?): AppResult<Unit> {
		return withResultTransaction {
			val existing = refreshTokenRepository.findById(id)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.NotFound, "Refresh token not found.")
			refreshTokenRepository.update(
				existing.copy(
					revokedAt = Instant.now(), replacedBy = replacedBy
				)
			)
			AppResult.Success(Unit)
		}
	}

	override suspend fun revokeAllByUserId(userId: UUID, replacedBy: UUID?): AppResult<Unit> {
		return withResultTransaction {
			val now = Instant.now()
			for (token in refreshTokenRepository.findAllByUserId(userId)) {
				refreshTokenRepository.update(
					token.copy(
						revokedAt = now, replacedBy = replacedBy
					)
				)
			}
			AppResult.Success(Unit)
		}
	}

	override suspend fun create(refreshToken: RefreshToken): AppResult<RefreshTokenJoined> {
		return withResultTransaction {
			val created = refreshTokenRepository.create(refreshToken)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to create refresh token.")
			val userResult = userService.getById(created.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(created.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}")
			}
		}
	}

	override suspend fun update(refreshToken: RefreshToken): AppResult<RefreshTokenJoined> {
		return withResultTransaction {
			val updated = refreshTokenRepository.update(refreshToken)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to update refresh token.")
			val userResult = userService.getById(updated.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(updated.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}")
			}
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return withResultTransaction {
			if (refreshTokenRepository.delete(id)) AppResult.Success(Unit)
			else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete refresh token.")
		}
	}
}
