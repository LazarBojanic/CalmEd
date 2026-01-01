package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import java.util.UUID

interface IAuthCredentialRepository {
	suspend fun findAll(): List<AuthCredential>
	suspend fun findById(id: UUID): AuthCredential?
	suspend fun findByUserIdAndType(userId: UUID, type: AuthCredentialType): AuthCredential?
	suspend fun findAllByUserId(userId: UUID): Set<AuthCredential>
	suspend fun create(authCredential: AuthCredential): AuthCredential?
	suspend fun update(authCredential: AuthCredential): AuthCredential?
	suspend fun delete(id: UUID): Boolean
}