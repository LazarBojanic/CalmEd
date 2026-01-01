package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import java.util.UUID

interface IRefreshTokenService {
	suspend fun getAll(): List<RefreshTokenJoined>
	suspend fun getById(id: UUID): RefreshTokenJoined?
	suspend fun create(refreshToken: RefreshToken): RefreshTokenJoined?
	suspend fun update(refreshToken: RefreshToken): RefreshTokenJoined?
	suspend fun delete(id: UUID): Boolean
}