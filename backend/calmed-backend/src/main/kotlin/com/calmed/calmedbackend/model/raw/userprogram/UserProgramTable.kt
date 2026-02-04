package com.calmed.calmedbackend.model.raw.userprogram

import com.calmed.calmedbackend.model.raw.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object UserProgramTable : UUIDTable("user_program") {
	val userId = uuid("user_id").references(UserTable.id)
	val startDate = date("start_date")
	val endDate = date("end_date").nullable()
	val timezone = text("timezone").nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
