package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.dbQuery
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
		return dbQuery { RefreshTokenEntity.all().map { it.toRaw() } }
	}

	override suspend fun findById(id: UUID): RefreshToken? {
		return dbQuery { RefreshTokenEntity.findById(id)?.toRaw() }
	}

	override suspend fun findAllByUserId(userId: UUID): List<RefreshToken> {
		return dbQuery {
			RefreshTokenEntity
				.find { RefreshTokenTable.userId eq userId }
				.map { it.toRaw() }
		}
	}

	override suspend fun create(refreshToken: RefreshToken): RefreshToken? {
		return dbQuery {
			RefreshTokenEntity.new(refreshToken.id) {
				setFrom(refreshToken, MapMode.CREATE)
			}.toRaw()
		}
	}

	override suspend fun update(refreshToken: RefreshToken): RefreshToken? {
		return 	dbQuery {
			val e = RefreshTokenEntity.findById(refreshToken.id) ?: return@dbQuery null
			e.setFrom(refreshToken, MapMode.UPDATE)
			e.toRaw()
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return dbQuery {
			val e = RefreshTokenEntity.findById(id) ?: return@dbQuery false
			e.delete(); true
		}
	}
}