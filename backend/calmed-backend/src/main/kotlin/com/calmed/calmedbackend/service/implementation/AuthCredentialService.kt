package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import com.calmed.calmedbackend.service.specification.IUserService
import java.util.UUID

class AuthCredentialService(
	private val authCredentialRepository: IAuthCredentialRepository,
	private val userService: IUserService
) : IAuthCredentialService {

	override suspend fun getAll(): AppResult<List<AuthCredentialJoined>> {
		val result = mutableListOf<AuthCredentialJoined>()

		for (authCredential in authCredentialRepository.findAll()) {
			val userResult = userService.getById(authCredential.userId)

			if (userResult is AppResult.Success) {
				result.add(authCredential.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}

		return AppResult.Success(result)
	}

	override suspend fun getById(id: UUID): AppResult<AuthCredentialJoined> {
		val authCredential = authCredentialRepository.findById(id)

		if (authCredential != null) {
			val userResult = userService.getById(authCredential.userId)

			if (userResult is AppResult.Success) {
				return AppResult.Success(authCredential.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}
		else {
			return AppResult.Failure("Failed to retrieve credentials.")
		}
	}

	override suspend fun getByUserIdAndType(
		userId: UUID,
		type: AuthCredentialType
	): AppResult<AuthCredentialJoined> {

		val authCredential = authCredentialRepository.findByUserIdAndType(userId, type)

		if (authCredential != null) {
			val userResult = userService.getById(authCredential.userId)

			if (userResult is AppResult.Success) {
				return AppResult.Success(authCredential.join(userResult.data))
			}
			else {
				return AppResult.Failure("Failed to retrieve user.")
			}
		}
		else {
			return AppResult.Failure("Failed to retrieve credentials.")
		}
	}

	override suspend fun create(authCredential: AuthCredential): AppResult<AuthCredentialJoined> {
		val created = authCredentialRepository.create(authCredential)

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
			return AppResult.Failure("Failed to create auth credential.")
		}
	}

	override suspend fun update(authCredential: AuthCredential): AppResult<AuthCredentialJoined> {
		val updated = authCredentialRepository.update(authCredential)

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
			return AppResult.Failure("Failed to update auth credential.")
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		val deleted = authCredentialRepository.delete(id)

		if (deleted) {
			return AppResult.Success(Unit)
		}
		else {
			return AppResult.Failure("Failed to delete credential.")
		}
	}
}
