package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.model.raw.message.Message
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import java.util.UUID

class AuthCredentialService(
	private val authCredentialRepository: IAuthCredentialRepository
) : IAuthCredentialService {
	override suspend fun getAll(): List<AuthCredentialJoined> {
		TODO("Not yet implemented")
	}

	override suspend fun getById(id: UUID): AuthCredentialJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun getByUserIdAndType(userId: UUID, type: AuthCredentialType): AuthCredentialJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun create(authCredential: AuthCredential): AuthCredentialJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun update(authCredential: AuthCredential): AuthCredentialJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun delete(id: UUID): Boolean {
		TODO("Not yet implemented")
	}
}