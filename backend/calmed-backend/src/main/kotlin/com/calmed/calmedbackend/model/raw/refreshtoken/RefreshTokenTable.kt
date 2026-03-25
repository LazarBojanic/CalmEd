package com.calmed.calmedbackend.model.raw.refreshtoken

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object RefreshTokenTable : UUIDTable("refresh_token") {
	val replacedBy = javaUUID("replaced_by").nullable()
	val userId = javaUUID("user_id").references(UserTable.id)
	val tokenHash = text("token_hash")
	val issuedAt = timestamp("issued_at").defaultExpression(CurrentTimestamp)
	val expiresAt = timestamp("expires_at")
	val revokedAt = timestamp("revoked_at").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
