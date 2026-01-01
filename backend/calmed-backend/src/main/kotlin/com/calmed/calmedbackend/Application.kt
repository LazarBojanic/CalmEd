package com.calmed.calmedbackend;

import com.calmed.calmedbackend.auth.configureSecurity
import com.calmed.calmedbackend.database.configureDatabase
import com.calmed.calmedbackend.di.configureFrameworks
import com.calmed.calmedbackend.http.configureHTTP
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialTable
import com.calmed.calmedbackend.model.raw.message.Message
import com.calmed.calmedbackend.model.raw.message.MessageTable
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshTokenTable
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.routing.configureRouting
import com.calmed.calmedbackend.service.specification.IMessageService
import com.calmed.calmedbackend.util.configureMonitoring
import com.calmed.calmedbackend.util.configureSerialization
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.module() {
	val dev: Boolean = environment.config.property("ktor.development").getString().toBoolean()
	configureFrameworks()
	configureHTTP()
	configureSecurity()
	configureSerialization()
	configureMonitoring()
	configureDatabase()
	configureRouting()

	val allTables = arrayOf(
		UserTable,
		AuthCredentialTable,
		RefreshTokenTable,
		MessageTable
	)
	transaction {
		if(dev){
			exec("DROP SCHEMA IF EXISTS public CASCADE;")
			exec("CREATE SCHEMA public;")
		}
	}
	transaction{
		SchemaUtils.createMissingTablesAndColumns(*allTables)
	}
	seed()

}

suspend fun Application.seed(){
	val message1: Message = Message.createNew("hello1")
	val message2: Message = Message.createNew("hello2")
	val message3: Message = Message.createNew("hello3")

	val messageService by inject<IMessageService>()
	messageService.create(message1)
	messageService.create(message2)
	messageService.create(message3)

}