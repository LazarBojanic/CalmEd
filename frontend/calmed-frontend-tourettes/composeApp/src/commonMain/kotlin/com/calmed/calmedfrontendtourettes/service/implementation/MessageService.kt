package com.calmed.calmedfrontendtourettes.service.implementation

import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.model.join
import com.calmed.calmedfrontendtourettes.model.joined.MessageJoined
import com.calmed.calmedfrontendtourettes.model.raw.Message
import com.calmed.calmedfrontendtourettes.model.toEntity
import com.calmed.calmedfrontendtourettes.model.toJoined
import com.calmed.calmedfrontendtourettes.model.toRaw
import com.calmed.calmedfrontendtourettes.repository.IMessageDao
import com.calmed.calmedfrontendtourettes.service.specification.IMessageService
import kotlinx.coroutines.flow.first

class MessageService(
	private val messageDao: IMessageDao,
	private val appApi: IAppApi
) : IMessageService {

	/* =========================
	   NETWORK (request*)
	   ========================= */

	override suspend fun requestGetAll(): List<MessageJoined> {
		return appApi.getAllMessages().map { it.toJoined() }
	}

	override suspend fun requestGetById(id: String): MessageJoined? {
		return appApi.getMessageById(id)?.toJoined()
	}

	override suspend fun requestCreate(message: Message): MessageJoined? {
		return appApi.createMessage(message)?.toJoined()
	}

	override suspend fun requestUpdate(message: Message): MessageJoined? {
		return appApi.updateMessage(message)?.toJoined()
	}

	override suspend fun requestDelete(id: String): Boolean {
		return appApi.deleteMessage(id)
	}

	/* =========================
	   LOCAL (database)
	   ========================= */

	override suspend fun getAll(): List<MessageJoined> {
		val local = messageDao.findAll().first()

		return if (local.isNotEmpty()) {
			local.map { it.join() }
		} else {
			val remote = requestGetAll()
			cache(remote)
			remote
		}
	}

	override suspend fun getById(id: String): MessageJoined? {
		val local = messageDao.findAll().first()
			.firstOrNull { it.id == id }

		return local?.join()
			?: requestGetById(id)?.also { cache(listOf(it)) }
	}

	override suspend fun create(message: Message): MessageJoined? {
		val created = requestCreate(message)
		created?.let { cache(listOf(it)) }
		return created
	}

	override suspend fun update(message: Message): MessageJoined? {
		val updated = requestUpdate(message)
		updated?.let { cache(listOf(it)) }
		return updated
	}

	override suspend fun delete(id: String): Boolean {
		val success = requestDelete(id)
		if (success) {
			messageDao.clearAll()
		}
		return success
	}

	/* =========================
	   INTERNAL
	   ========================= */

	private suspend fun cache(messages: List<MessageJoined>) {
		messages.forEach { message ->
			messageDao.upsert(message.toEntity())
		}
	}
}