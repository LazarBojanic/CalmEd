package com.calmed.calmedbackend.model.raw.userinfo.tics

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object UserInfoTicsTable : UUIDTable(name = "user_info_tics") {
	val userId = javaUUID("user_id").references(UserTable.id)
	val preferredName = varchar("preferred_name", 255).nullable()
	val age = integer("age").nullable()
	val stressLevel = integer("stress_level").nullable()
	val tickType = enumeration("tick_type", TickType::class).nullable()
	val tickFrequency = enumeration("tick_frequency", TickFrequency::class).nullable()
	val goal = text("goal").nullable()
	val followProgress = bool("follow_progress").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
