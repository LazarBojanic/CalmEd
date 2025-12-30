package com.calmed.calmedbackend.model.raw.message

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID


class MessageEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<MessageEntity>(MessageTable)
	var text by MessageTable.text
	var createdAt by MessageTable.createdAt
	var updatedAt by MessageTable.updatedAt
}