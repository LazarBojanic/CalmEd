package com.calmed.calmedbackend.model.raw.user

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object UserTable : UUIDTable(name = "user") {
	val email = varchar("email", 255).uniqueIndex()
	val username = varchar("username", 255).uniqueIndex()
	val isEmailVerified = bool("is_email_verified").default(false)
	val isOnboarded = bool("is_onboarded").default(false)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}