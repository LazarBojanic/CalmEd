package com.calmed.calmedbackend.database

import com.calmed.calmedbackend.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager.Companion.currentOrNull
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
	val databaseConfig by inject<DatabaseConfig>()
	val dataSource = dataSource(databaseConfig)
	Database.connect(
		dataSource
	)
}

private fun dataSource(databaseConfig: DatabaseConfig): HikariDataSource {
	val config = HikariConfig()
	config.driverClassName = databaseConfig.databaseDriver
	config.jdbcUrl = databaseConfig.databaseUrl
	config.username = databaseConfig.databaseUsername
	config.password = databaseConfig.databasePassword
	config.maximumPoolSize = 3
	config.isAutoCommit = false
	config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
	config.validate()
	return HikariDataSource(config)
}

suspend fun <T> withTransaction(block: suspend () -> T): T {
	return suspendTransaction { block() }
}




