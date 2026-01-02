package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.tx
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
		return tx {
			RefreshTokenEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): RefreshToken? {
		return tx {
			val e = RefreshTokenEntity.findById(id)
			if (e != null) {
				return@tx e.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun findAllByUserId(userId: UUID): List<RefreshToken> {
		return tx {
			RefreshTokenEntity
				.find { RefreshTokenTable.userId eq userId }
				.map { it.toRaw() }
		}
	}

	override suspend fun create(refreshToken: RefreshToken): RefreshToken? {
		return tx {
			val existing = RefreshTokenEntity.findById(refreshToken.id)
			if (existing == null) {
				return@tx RefreshTokenEntity.new(refreshToken.id) {
					setFrom(refreshToken, MapMode.CREATE)
				}.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun update(refreshToken: RefreshToken): RefreshToken? {
		return tx {
			val e = RefreshTokenEntity.findById(refreshToken.id)
			if (e != null) {
				e.setFrom(refreshToken, MapMode.UPDATE)
				return@tx e.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return tx {
			val e = RefreshTokenEntity.findById(id)
			if (e != null) {
				e.delete()
				return@tx true
			}
			else {
				return@tx false
			}
		}
	}
}
