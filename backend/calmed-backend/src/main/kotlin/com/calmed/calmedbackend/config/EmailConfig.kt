package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class EmailConfig(
	val email: String,
	val host: String,
	val port: Int,
	val password: String,
	val ssl: Boolean
) {
	companion object {
		fun from(config: ApplicationConfig): EmailConfig {
			return EmailConfig(
				host = config.property("email.host").getString(),
				port = config.property("email.port").getString().toInt(),
				email = config.property("email.email").getString(),
				password = config.property("email.password").getString(),
				ssl = config.property("email.ssl").getString().toBoolean()
			)
		}
	}
}