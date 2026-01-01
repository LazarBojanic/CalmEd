package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import java.util.UUID

interface IRefreshTokenRepository {
	suspend fun findAll(): List<RefreshToken>
	suspend fun findById(id: UUID): RefreshToken?
	suspend fun findAllByUserId(userId: UUID): List<RefreshToken>
	suspend fun findByTokenHash(tokenHash: String): RefreshToken?
	suspend fun create(refreshToken: RefreshToken): RefreshToken?
	suspend fun update(refreshToken: RefreshToken): RefreshToken?
	suspend fun delete(id: UUID): Boolean
}