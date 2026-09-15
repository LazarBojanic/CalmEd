package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenEntity
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IRefreshTokenRepository
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class RefreshTokenRepository : IRefreshTokenRepository {

	override suspend fun findAll(): List<RefreshToken> {
		return RefreshTokenEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): RefreshToken? {
		return RefreshTokenEntity.findById(id)?.toRaw()
	}

	override suspend fun findAllByUserId(userId: UUID): List<RefreshToken> {
		return RefreshTokenEntity
			.find { RefreshTokenTable.userId eq userId }
			.map { it.toRaw() }
	}

	override suspend fun create(refreshToken: RefreshToken): RefreshToken? {
		if (RefreshTokenEntity.findById(refreshToken.id) != null) return null
		return RefreshTokenEntity.new(refreshToken.id) {
			setFrom(refreshToken, MapMode.CREATE)
		}.toRaw()
	}

	override suspend fun update(refreshToken: RefreshToken): RefreshToken? {
		val e = RefreshTokenEntity.findById(refreshToken.id) ?: return null
		e.setFrom(refreshToken, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = RefreshTokenEntity.findById(id) ?: return false
		e.delete()
		return true
	}

	override suspend fun deleteByUserId(userId: UUID): Boolean {
		val entities = RefreshTokenEntity.find {
			RefreshTokenTable.userId eq userId
		}
		var deleted = false
		for (e in entities) {
			e.delete()
			deleted = true
		}
		return deleted
	}
}
