package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import java.util.UUID

class RefreshTokenService : IRefreshTokenService {
	override suspend fun getAll(): List<RefreshTokenJoined> {
		TODO("Not yet implemented")
	}

	override suspend fun getById(id: UUID): RefreshTokenJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun create(refreshToken: RefreshToken): RefreshTokenJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun update(refreshToken: RefreshToken): RefreshTokenJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun delete(id: UUID): Boolean {
		TODO("Not yet implemented")
	}
}