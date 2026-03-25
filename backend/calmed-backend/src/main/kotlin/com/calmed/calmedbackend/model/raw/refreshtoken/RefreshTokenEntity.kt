package com.calmed.calmedbackend.model.raw.refreshtoken

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.UUID

class RefreshTokenEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<RefreshTokenEntity>(RefreshTokenTable)
	var replacedBy by RefreshTokenTable.replacedBy
	var userId by RefreshTokenTable.userId
	var tokenHash by RefreshTokenTable.tokenHash
	var issuedAt by RefreshTokenTable.issuedAt
	var expiresAt by RefreshTokenTable.expiresAt
	var revokedAt by RefreshTokenTable.revokedAt
	var createdAt by RefreshTokenTable.createdAt
	var updatedAt by RefreshTokenTable.updatedAt
}