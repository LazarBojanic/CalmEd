package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class EmailConfig(
	val host: String,
	val port: Int,
	val username: String,
	val password: String,
	val ssl: Boolean,
	val fromEmail: String,
	val fromName: String,
	val verificationBaseUrl: String
) {
	companion object {
		fun from(config: ApplicationConfig): EmailConfig {
			return EmailConfig(
				host = config.property("email.host").getString(),
				port = config.property("email.port").getString().toInt(),
				username = config.property("email.username").getString(),
				password = config.property("email.password").getString(),
				ssl = config.property("email.ssl").getString().toBoolean(),
				fromEmail = config.property("email.from_email").getString(),
				fromName = config.property("email.from_name").getString(),
				verificationBaseUrl = config.property("email.verification_base_url").getString()
			)
		}
	}
}