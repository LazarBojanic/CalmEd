package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class KtorConfig(
	val deploymentHost: String,
	val deploymentPort: Int,
	val development: Boolean,
	) {
	companion object {
		fun from(config: ApplicationConfig): KtorConfig {
			return KtorConfig(
				deploymentHost = config.property("ktor.deployment.host").getString(),
				deploymentPort = config.property("ktor.deployment.port").getString().toInt(),
				development = config.property("ktor.development").getString().toBoolean(),
			)
		}
	}
}