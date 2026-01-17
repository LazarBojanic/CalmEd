package com.calmed.calmedbackend.model.raw.authcredential

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

class AuthCredentialEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<AuthCredentialEntity>(AuthCredentialTable)
	var userId by AuthCredentialTable.userId
	var type by AuthCredentialTable.type
	var passwordHash by AuthCredentialTable.passwordHash
	var createdAt by AuthCredentialTable.createdAt
	var updatedAt by AuthCredentialTable.updatedAt
	var providerUserId by AuthCredentialTable.providerUserId
}