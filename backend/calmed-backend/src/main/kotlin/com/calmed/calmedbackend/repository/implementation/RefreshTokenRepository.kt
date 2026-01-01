package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import java.util.UUID

class RefreshTokenRepository : IRefreshTokenRepository {
	override suspend fun findAll(): List<RefreshToken> {
		TODO("Not yet implemented")
	}

	override suspend fun findById(id: UUID): RefreshToken? {
		TODO("Not yet implemented")
	}

	override suspend fun create(refreshToken: RefreshToken): RefreshToken? {
		TODO("Not yet implemented")
	}

	override suspend fun update(refreshToken: RefreshToken): RefreshToken? {
		TODO("Not yet implemented")
	}

	override suspend fun delete(id: UUID): Boolean {
		TODO("Not yet implemented")
	}
}