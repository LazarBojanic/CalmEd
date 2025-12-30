package com.calmed.calmedbackend.database

import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

fun Application.configureDatabase() {
	val databaseName = environment.config.property("database.name").getString()
	val databaseUsername = environment.config.property("database.username").getString()
	val databasePassword = environment.config.property("database.password").getString()
	val databaseIP = environment.config.property("database.ip").getString()
	val databasePort = environment.config.property("database.port").getString().toInt()
	val databaseDialect = environment.config.property("database.dialect").getString()
	val databaseDriver = environment.config.property("database.driver").getString()
	val databaseUrl = String.format("jdbc:%s://%s/%s", databaseDialect, databaseIP, databaseName)
	Database.connect(
		url = databaseUrl,
		user = databaseUsername,
		driver = databaseDriver,
		password = databasePassword,
	)
}
suspend fun <T> dbQuery(block: suspend () -> T): T {
	return suspendTransaction{
		block()
	}
}





