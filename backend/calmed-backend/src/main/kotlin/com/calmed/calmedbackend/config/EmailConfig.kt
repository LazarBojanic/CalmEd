package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class EmailConfig(
	val host: String,
	val port: Int,
	val username: String,
	val password: String,
	val ssl: Boolean,
	val appEmail: String,
	val supportEmail: String,
	val verificationBaseUrl: String,
	val mailtrapApiToken: String,
	val mailtrapInboxId: Long
) {
	companion object {
		fun from(config: ApplicationConfig): EmailConfig {
			return EmailConfig(
				host = config.property("email.host").getString(),
				port = config.property("email.port").getString().toInt(),
				username = config.property("email.username").getString(),
				password = config.property("email.password").getString(),
				ssl = config.property("email.ssl").getString().toBoolean(),
				appEmail = config.property("email.app_email").getString(),
				supportEmail = config.property("email.support_email").getString(),
				verificationBaseUrl = config.property("email.verification_base_url").getString(),
				mailtrapApiToken = config.property("email.mailtrap_api_token").getString(),
				mailtrapInboxId = config.property("email.mailtrap_inbox_id").getString().toLong()
			)
		}
	}
}