package com.calmed.calmedbackend.model.raw.message

import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import java.time.Instant

object MessageTable : UUIDTable(name = "message") {
	val text = text("text").nullable()
	val createdAt = timestamp("created_at").default(Instant.now())
	val updatedAt = timestamp("updated_at").default(Instant.now())
}