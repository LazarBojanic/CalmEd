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
	val dataSource = hikariDataSource(databaseConfig)
	Database.connect(
		dataSource
	)
}

private fun hikariDataSource(databaseConfig: DatabaseConfig): HikariDataSource {
    val config = HikariConfig().apply {
        driverClassName = databaseConfig.databaseDriver
        jdbcUrl = databaseConfig.databaseUrl

        // Credentials handled by Hikari
        username = databaseConfig.databaseUsername
        password = databaseConfig.databasePassword

        // Neon-safe defaults
        maximumPoolSize = 5
        minimumIdle = 1

        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        connectionTimeout = 10_000
        initializationFailTimeout = 10_000
    }

    return HikariDataSource(config)
}

suspend fun <T> withTransaction(block: suspend () -> T): T {
	return suspendTransaction { block() }
}




