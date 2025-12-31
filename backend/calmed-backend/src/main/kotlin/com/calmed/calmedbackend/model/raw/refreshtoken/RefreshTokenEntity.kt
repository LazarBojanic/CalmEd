package com.calmed.calmedbackend.model.raw.refreshtoken

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

class RefreshTokenEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<RefreshTokenEntity>(RefreshTokenTable)
	var userId by RefreshTokenTable.userId
	val tokenHash by RefreshTokenTable.tokenHash
	val issuedAt by RefreshTokenTable.issuedAt
	val expiresAt by RefreshTokenTable.expiresAt
	val revokedAt by RefreshTokenTable.revokedAt
	val createdAt by RefreshTokenTable.createdAt
	val updatedAt by RefreshTokenTable.updatedAt
}