package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.dbQuery
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.message.Message
import com.calmed.calmedbackend.model.raw.message.MessageEntity
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IMessageRepository
import java.time.Instant
import java.util.UUID

class MessageRepository : IMessageRepository {
	override suspend fun findAll(): List<Message> {
		return dbQuery {
			MessageEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): Message? {
		return dbQuery {
			MessageEntity.findById(id)?.toRaw()
		}
	}

	override suspend fun create(message: Message): Message? {
		return dbQuery {
			MessageEntity.new(message.id) {
				setFrom(message, MapMode.CREATE)
			}.toRaw()
		}
	}

	override suspend fun update(message: Message): Message? {
		return dbQuery {
			val e = MessageEntity.findById(message.id) ?: return@dbQuery null
			e.updatedAt = Instant.now()
			e.setFrom(message.copy(updatedAt = e.updatedAt), MapMode.UPDATE)
			e.toRaw()
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return dbQuery {
			val e = MessageEntity.findById(id) ?: return@dbQuery false
			e.delete(); true
		}
	}
}