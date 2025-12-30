package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.message.Message
import java.util.UUID

interface IMessageRepository {
	suspend fun findAll(): List<Message>
	suspend fun findById(id: UUID): Message?
	suspend fun create(message: Message): Message?
	suspend fun update(message: Message): Message?
	suspend fun delete(id: UUID): Boolean
}