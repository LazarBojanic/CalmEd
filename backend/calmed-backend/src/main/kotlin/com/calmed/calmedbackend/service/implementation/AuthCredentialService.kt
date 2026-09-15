package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.util.UUID

class AuthCredentialService(private val authCredentialRepository: IAuthCredentialRepository,
                            private val userService: IUserService
) : IAuthCredentialService {
	override suspend fun getAll(): AppResult<List<AuthCredentialJoined>> {
		return withTransaction {
			val usersById = when (val usersResult = userService.getAll()) {
				is AppResult.Success -> usersResult.data.associateBy { it.id }
				is AppResult.Failure -> return@withTransaction AppResult.Failure(
					usersResult.httpStatusCode, "Failed to retrieve users. ${usersResult.message}"
				)
			}

			val result = mutableListOf<AuthCredentialJoined>()
			for (authCredential in authCredentialRepository.findAll()) {
				val user = usersById[authCredential.userId]
					?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to retrieve user.")
				result.add(authCredential.join(user))
			}

			AppResult.Success(result)
		}
	}

	override suspend fun getById(id: UUID): AppResult<AuthCredentialJoined> {
		return withTransaction {
			val authCredential = authCredentialRepository.findById(id)
				?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to retrieve credentials.")
			val userResult = userService.getById(authCredential.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(authCredential.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}

	override suspend fun getByUserIdAndType(userId: UUID, type: AuthCredentialType
	): AppResult<AuthCredentialJoined> {
		return withTransaction {
			val authCredential = authCredentialRepository.findByUserIdAndType(userId, type)
				?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to retrieve credentials.")
			val userResult = userService.getById(authCredential.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(authCredential.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode, "Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}

	override suspend fun findRawByProviderUserIdAndType(
		providerUserId: String,
		type: AuthCredentialType
	): AuthCredential? {
		return withTransaction {
			authCredentialRepository.findByProviderUserIdAndType(providerUserId, type)
		}
	}

	override suspend fun create(authCredential: AuthCredential): AppResult<AuthCredentialJoined> {
		return withResultTransaction {
			val created = authCredentialRepository.create(authCredential)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to create auth credential.")
			val userResult = userService.getById(created.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(created.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(userResult.httpStatusCode, "Failed to create user. ${userResult.message}")
			}
		}
	}

	override suspend fun update(authCredential: AuthCredential): AppResult<AuthCredentialJoined> {
		return withResultTransaction {
			val updated = authCredentialRepository.update(authCredential)
				?: return@withResultTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to update auth credential.")
			val userResult = userService.getById(updated.userId)
			when (userResult) {
				is AppResult.Success -> AppResult.Success(updated.join(userResult.data))
				is AppResult.Failure -> AppResult.Failure(
					userResult.httpStatusCode,
					"Failed to retrieve user. ${userResult.message}"
				)
			}
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return withResultTransaction {
			if (authCredentialRepository.delete(id)) AppResult.Success(Unit)
			else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete credential.")
		}
	}
}
