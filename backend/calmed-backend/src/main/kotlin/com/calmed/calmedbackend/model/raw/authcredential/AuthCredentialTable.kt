package com.calmed.calmedbackend.model.raw.authcredential

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object AuthCredentialTable : UUIDTable("auth_credential") {
	val userId = uuid("user_id").references(UserTable.id)
	val type = enumeration("type", AuthCredentialType::class)
	val passwordHash = text(name = "password_hash").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
	val providerUserId = text(name = "provider_user_id").nullable()
}

