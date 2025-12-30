package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.joined.MessageJoined
import com.calmed.calmedbackend.model.raw.message.Message
import java.util.UUID

interface IMessageService {
	suspend fun getAll(): List<MessageJoined>
	suspend fun getById(id: UUID): MessageJoined?
	suspend fun create(message: Message): MessageJoined?
	suspend fun update(message: Message): MessageJoined?
	suspend fun delete(id: UUID): Boolean
}