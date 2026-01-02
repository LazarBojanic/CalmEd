package com.calmed.calmedbackend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

data class EnvironmentDatabaseConfig(
	val databaseName: String,
	val databaseUsername: String,
	val databasePassword: String,
	val databaseIP: String,
	val databasePort: Int,
	val databaseDialect: String,
	val databaseDriver: String,
	val databaseUrl: String
)

fun Application.configureDatabase() {
	val databaseName = environment.config.property("database.name").getString()
	val databaseUsername = environment.config.property("database.username").getString()
	val databasePassword = environment.config.property("database.password").getString()
	val databaseIP = environment.config.property("database.ip").getString()
	val databasePort = environment.config.property("database.port").getString().toInt()
	val databaseDialect = environment.config.property("database.dialect").getString()
	val databaseDriver = environment.config.property("database.driver").getString()
	val databaseUrl = String.format("jdbc:%s://%s/%s", databaseDialect, databaseIP, databaseName)
	val environmentDatabaseConfig = EnvironmentDatabaseConfig(
		databaseName,
		databaseUsername,
		databasePassword,
		databaseIP,
		databasePort,
		databaseDialect,
		databaseDriver,
		databaseUrl
	)
	val dataSource = dataSource(environmentDatabaseConfig)
	Database.connect(
		dataSource
	)
}

private fun dataSource(environmentDatabaseConfig: EnvironmentDatabaseConfig): HikariDataSource {
	val config = HikariConfig()
	config.driverClassName = environmentDatabaseConfig.databaseDriver
	config.jdbcUrl = environmentDatabaseConfig.databaseUrl
	config.username = environmentDatabaseConfig.databaseUsername
	config.password = environmentDatabaseConfig.databasePassword
	config.maximumPoolSize = 3
	config.isAutoCommit = false
	config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
	config.validate()
	return HikariDataSource(config)
}

suspend fun <T> tx(block: suspend () -> T): T {
	return if (TransactionManager.currentOrNull() != null) {
		block()
	}
	else {
		suspendTransaction { block() }
	}
}





