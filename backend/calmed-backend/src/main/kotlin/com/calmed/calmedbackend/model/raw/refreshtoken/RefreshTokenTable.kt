package com.calmed.calmedbackend.model.raw.refreshtoken

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object RefreshTokenTable : UUIDTable("refresh_token") {
	val userId = uuid("user_id").references(UserTable.id)
	val tokenHash = text("token_hash")
	val issuedAt = timestamp("issued_at").default(Instant.now())
	val expiresAt = timestamp("expires_at")
	val revokedAt = timestamp("revoked_at").nullable()
	val createdAt = timestamp("created_at").default(Instant.now())
	val updatedAt = timestamp("updated_at").default(Instant.now())
}