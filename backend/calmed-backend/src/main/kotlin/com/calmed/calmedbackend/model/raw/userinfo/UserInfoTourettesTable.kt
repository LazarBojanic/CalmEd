package com.calmed.calmedbackend.model.raw.userinfo

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object UserInfoTourettesTable : UUIDTable(name = "user_info_tourettes") {
	val userId = uuid("user_id").references(UserTable.id)
	val preferredName = varchar("preferred_name", 255).nullable()
	val age = integer("age").nullable()
	val stressLevel = integer("stress_level").nullable()
	val tickType = enumeration("tick_type", TickType::class).nullable()
	val tickFrequency = enumeration("tick_frequency", TickFrequency::class).nullable()
	val goal = text("goal").nullable()
	val followProgress = bool("follow_progress").nullable()
	val createdAt = timestamp("created_at").default(Instant.now())
	val updatedAt = timestamp("updated_at").default(Instant.now())
}