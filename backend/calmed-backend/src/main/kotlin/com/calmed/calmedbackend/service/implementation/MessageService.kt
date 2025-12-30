package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.MessageJoined
import com.calmed.calmedbackend.model.raw.message.Message
import com.calmed.calmedbackend.repository.specification.IMessageRepository
import com.calmed.calmedbackend.service.specification.IMessageService
import java.util.UUID

class MessageService(
	private val messageRepository: IMessageRepository,
) : IMessageService {
	override suspend fun getAll(): List<MessageJoined> {
		return messageRepository.findAll().map { it.join() }
	}

	override suspend fun getById(id: UUID): MessageJoined? {
		return messageRepository.findById(id)?.join()
	}

	override suspend fun create(message: Message): MessageJoined? {
		return messageRepository.create(message)?.join()
	}

	override suspend fun update(message: Message): MessageJoined? {
		return messageRepository.update(message)?.join()
	}

	override suspend fun delete(id: UUID): Boolean {
		return messageRepository.delete(id)
	}
}