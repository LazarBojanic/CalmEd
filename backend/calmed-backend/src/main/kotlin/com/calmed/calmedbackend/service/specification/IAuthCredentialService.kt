package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import java.util.UUID

interface IAuthCredentialService {
	suspend fun getAll(): List<AuthCredentialJoined>
	suspend fun getById(id: UUID): AuthCredentialJoined?
	suspend fun getByUserIdAndType(userId: UUID, type: AuthCredentialType): AuthCredentialJoined?
	suspend fun create(authCredential: AuthCredential): AuthCredentialJoined?
	suspend fun update(authCredential: AuthCredential): AuthCredentialJoined?
	suspend fun delete(id: UUID): Boolean
}