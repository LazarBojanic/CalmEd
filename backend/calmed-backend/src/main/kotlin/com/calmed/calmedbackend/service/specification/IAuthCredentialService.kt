package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.joined.AuthCredentialJoined
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import java.util.UUID

interface IAuthCredentialService {
	suspend fun getAll(): AppResult<List<AuthCredentialJoined>>
	suspend fun getById(id: UUID): AppResult<AuthCredentialJoined>
	suspend fun getByUserIdAndType(userId: UUID, type: AuthCredentialType): AppResult<AuthCredentialJoined>
	suspend fun create(authCredential: AuthCredential): AppResult<AuthCredentialJoined>
	suspend fun update(authCredential: AuthCredential): AppResult<AuthCredentialJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
}