package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IMessageService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.messageRoutes() {
	val messageService by inject<IMessageService>()
	route("/message/get-all") {
		get {
			val messagesJoined = messageService.getAll()
			val messageDtos = messagesJoined.map { it.toDto() }
			call.respond(messageDtos)
		}
	}
}