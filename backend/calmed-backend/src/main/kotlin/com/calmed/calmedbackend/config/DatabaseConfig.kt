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
	val sslMode: String,
	val channelBinding: String,
	val databaseUrl: String,
) {
	companion object {
		fun from(config: ApplicationConfig): DatabaseConfig {
			val databaseName = config.property("database.name").getString()
			val databaseIP = config.property("database.ip").getString()
			val databaseDialect = config.property("database.dialect").getString()
			val databaseUsername = config.property("database.username").getString()
			val databasePassword = config.property("database.password").getString()
			var sslMode = ""
			var channelBinding = ""
			try{
				sslMode = config.property("ssl.mode").getString()
				channelBinding = config.property("ssl.channel").getString()
			}
			catch (e: Exception){
				println(e.message)
			}
			val sslModeAndChannelBinding = sslModeAndChannelBinding(sslMode, channelBinding)
			return DatabaseConfig(
				databaseName = databaseName,
				databaseUsername = databaseUsername,
				databasePassword = databasePassword,
				databaseIP = databaseIP,
				databasePort = config.property("database.port").getString().toInt(),
				databaseDialect = databaseDialect,
				databaseDriver = config.property("database.driver").getString(),
				sslMode = sslMode,
				channelBinding = channelBinding,
				databaseUrl = String.format("jdbc:%s://%s/%s?user=%s&password=%s%s", databaseDialect, databaseIP, databaseName, databaseUsername, databasePassword, sslModeAndChannelBinding)
			)
		}
		private fun sslModeAndChannelBinding(sslMode: String, channelBinding: String): String {
			if(sslMode.isEmpty() && channelBinding.isEmpty()){
				return ""
			}
			else{
				return "&sslMode=$sslMode&channelBinding=$channelBinding"
			}
		}
	}
}