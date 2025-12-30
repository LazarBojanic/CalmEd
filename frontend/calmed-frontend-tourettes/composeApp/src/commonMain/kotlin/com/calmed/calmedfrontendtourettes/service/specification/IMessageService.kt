package com.calmed.calmedfrontendtourettes.service.specification

import com.calmed.calmedfrontendtourettes.model.joined.MessageJoined
import com.calmed.calmedfrontendtourettes.model.raw.Message

interface IMessageService {
	suspend fun requestGetAll(): List<MessageJoined>
	suspend fun requestGetById(id: String): MessageJoined?
	suspend fun requestCreate(message: Message): MessageJoined?
	suspend fun requestUpdate(message: Message): MessageJoined?
	suspend fun requestDelete(id: String): Boolean

	suspend fun getAll(): List<MessageJoined>
	suspend fun getById(id: String): MessageJoined?
	suspend fun create(message: Message): MessageJoined?
	suspend fun update(message: Message): MessageJoined?
	suspend fun delete(id: String): Boolean
}