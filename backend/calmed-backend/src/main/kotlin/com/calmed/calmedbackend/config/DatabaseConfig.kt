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
	val databaseUrl: String,
	val useFlyway: Boolean,
	val recreate: Boolean,
) {
	companion object {
		fun from(config: ApplicationConfig): DatabaseConfig {
			val databaseName = config.property("database.name").getString()
			val databaseIP = config.property("database.ip").getString()
			val databaseDialect = config.property("database.dialect").getString()
			val databaseUsername = config.property("database.username").getString()
			val databasePassword = config.property("database.password").getString()
			val databasePort = config.property("database.port").getString().toInt()
			return DatabaseConfig(
				databaseName = databaseName,
				databaseUsername = databaseUsername,
				databasePassword = databasePassword,
				databaseIP = databaseIP,
				databasePort = databasePort,
				databaseDialect = databaseDialect,
				databaseDriver = config.property("database.driver").getString(),
				databaseUrl = "jdbc:$databaseDialect://$databaseIP:$databasePort/$databaseName",
				useFlyway = config.property("database.use_flyway").getString().toBoolean(),
				recreate = config.property("database.recreate").getString().toBoolean(),
			)
		}

	}
}