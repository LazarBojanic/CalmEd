package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import java.util.UUID

interface IRefreshTokenService {
	suspend fun getAll(): AppResult<List<RefreshTokenJoined>>
	suspend fun getById(id: UUID): AppResult<RefreshTokenJoined>
	suspend fun getAllByUserId(userId: UUID): AppResult<List<RefreshTokenJoined>>
	suspend fun revokeById(id: UUID, replacedBy: UUID?): AppResult<Unit>
	suspend fun revokeAllByUserId(userId: UUID, replacedBy: UUID?): AppResult<Unit>
	suspend fun create(refreshToken: RefreshToken): AppResult<RefreshTokenJoined>
	suspend fun update(refreshToken: RefreshToken): AppResult<RefreshTokenJoined>
	suspend fun delete(id: UUID): AppResult<Unit>
}
