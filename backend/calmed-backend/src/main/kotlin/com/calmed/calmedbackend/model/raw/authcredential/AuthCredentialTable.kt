package com.calmed.calmedbackend.model.raw.authcredential

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object AuthCredentialTable : UUIDTable("auth_credential") {
	val userId = uuid("user_id").references(UserTable.id)
	val type = enumeration("type", AuthCredentialType::class)
	val passwordHash = text("password_hash")
	val createdAt = timestamp("created_at").default(Instant.now())
	val updatedAt = timestamp("updated_at").default(Instant.now())
}

