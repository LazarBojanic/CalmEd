package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.repository.specification.IAuthCredentialRepository
import java.util.UUID

class AuthCredentialRepository : IAuthCredentialRepository {
	override suspend fun findAll(): List<AuthCredential> {
		TODO("Not yet implemented")
	}

	override suspend fun findById(id: UUID): AuthCredential? {
		TODO("Not yet implemented")
	}

	override suspend fun findAllByUserId(userId: String): Set<AuthCredential> {
		TODO("Not yet implemented")
	}

	override suspend fun create(authCredential: AuthCredential): AuthCredential? {
		TODO("Not yet implemented")
	}

	override suspend fun update(authCredential: AuthCredential): AuthCredential? {
		TODO("Not yet implemented")
	}

	override suspend fun delete(id: UUID): Boolean {
		TODO("Not yet implemented")
	}
}