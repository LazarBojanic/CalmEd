package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.RefreshTokenJoined
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import com.calmed.calmedbackend.service.specification.IUserService
import java.time.Instant
import java.util.UUID

class RefreshTokenService(
	private val refreshTokenRepository: IRefreshTokenRepository,
	private val userService: IUserService
) : IRefreshTokenService {
	override suspend fun getAll(): List<RefreshTokenJoined> {
		val result = mutableListOf<RefreshTokenJoined>()
		for (token in refreshTokenRepository.findAll()) {
			val user = userService.getById(token.userId) ?: continue
			result.add(token.join(user))
		}
		return result
	}

	override suspend fun getById(id: UUID): RefreshTokenJoined? {
		val token = refreshTokenRepository.findById(id) ?: return null
		val user = userService.getById(token.userId) ?: return null
		return token.join(user)
	}

	override suspend fun getAllByUserId(userId: UUID): List<RefreshTokenJoined> {
		val result = mutableListOf<RefreshTokenJoined>()
		val existing = refreshTokenRepository.findAllByUserId(userId)
		for (token in existing) {
			val user = userService.getById(token.userId) ?: continue
			result.add(token.join(user))
		}
		return result
	}

	override suspend fun revokeById(id: UUID, replacedBy: UUID?): Boolean {
		val existing = refreshTokenRepository.findById(id)
		if(existing != null) {
			update(existing.copy(revokedAt = Instant.now(), replacedBy = replacedBy))
			return true
		}
		return false
	}

	override suspend fun revokeAllByUserId(userId: UUID, replacedBy: UUID?): Boolean {
		try{
			val now = Instant.now()
			val existing = refreshTokenRepository.findAllByUserId(userId)
			for(token in existing){
				update(token.copy(revokedAt = now, replacedBy = replacedBy))
			}
			return true
		}
		catch(e: Exception){
			return false
		}
	}

	override suspend fun create(refreshToken: RefreshToken): RefreshTokenJoined? {
		val created = refreshTokenRepository.create(refreshToken) ?: return null
		val user = userService.getById(created.userId) ?: return null
		return created.join(user)
	}

	override suspend fun update(refreshToken: RefreshToken): RefreshTokenJoined? {
		val updated = refreshTokenRepository.update(refreshToken) ?: return null
		val user = userService.getById(updated.userId) ?: return null
		return updated.join(user)
	}

	override suspend fun delete(id: UUID): Boolean {
		return refreshTokenRepository.delete(id)
	}
}