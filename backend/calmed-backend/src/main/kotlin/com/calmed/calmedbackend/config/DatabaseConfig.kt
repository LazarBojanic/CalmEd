package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class DatabaseConfig(
	val databaseName: String,
	val databaseUsername: String,
	val databasePassword: String,
	val databaseIP: String,
	val databasePort: Int,
	val databaseDialect: String,
	val databaseDriver: String,
	val databaseUrl: String
) {
	companion object {
		fun from(config: ApplicationConfig): DatabaseConfig {
			val databaseName = config.property("database.name").getString()
			val databaseIP = config.property("database.ip").getString()
			val databaseDialect = config.property("database.dialect").getString()
			return DatabaseConfig(
				databaseName = databaseName,
				databaseUsername = config.property("database.username").getString(),
				databasePassword = config.property("database.password").getString(),
				databaseIP = databaseIP,
				databasePort = config.property("database.port").getString().toInt(),
				databaseDialect = databaseDialect,
				databaseDriver = config.property("database.driver").getString(),
				databaseUrl = String.format("jdbc:%s://%s/%s", databaseDialect, databaseIP, databaseName)
			)
		}
	}
}