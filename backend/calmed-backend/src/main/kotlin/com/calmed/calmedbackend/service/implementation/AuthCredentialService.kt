package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.model.raw.message.Message
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import com.calmed.calmedbackend.service.specification.IUserService
import java.util.UUID

class AuthCredentialService(
	private val authCredentialRepository: IAuthCredentialRepository,
	private val userService: IUserService
) : IAuthCredentialService {

	override suspend fun getAll(): List<AuthCredentialJoined> {
		val result = mutableListOf<AuthCredentialJoined>()
		for (cred in authCredentialRepository.findAll()) {
			val user = userService.getById(cred.userId) ?: continue
			result.add(cred.join(user))
		}
		return result
	}

	override suspend fun getById(id: UUID): AuthCredentialJoined? {
		val cred = authCredentialRepository.findById(id) ?: return null
		val user = userService.getById(cred.userId) ?: return null
		return cred.join(user)
	}

	override suspend fun getByUserIdAndType(
		userId: UUID,
		type: AuthCredentialType
	): AuthCredentialJoined? {
		val cred = authCredentialRepository.findByUserIdAndType(userId, type) ?: return null
		val user = userService.getById(userId) ?: return null
		return cred.join(user)
	}

	override suspend fun create(authCredential: AuthCredential): AuthCredentialJoined? {
		val created = authCredentialRepository.create(authCredential) ?: return null
		val user = userService.getById(created.userId) ?: return null
		return created.join(user)
	}

	override suspend fun update(authCredential: AuthCredential): AuthCredentialJoined? {
		val updated = authCredentialRepository.update(authCredential) ?: return null
		val user = userService.getById(updated.userId) ?: return null
		return updated.join(user)
	}

	override suspend fun delete(id: UUID): Boolean {
		return authCredentialRepository.delete(id)
	}
}