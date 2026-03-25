package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class MuxConfig(
	val signingKey: String,
	val privateKey: String,
) {
	companion object {
		fun from(config: ApplicationConfig): MuxConfig {
			return MuxConfig(
				signingKey = config.property("mux.signing_key").getString(),
				privateKey = config.property("mux.private_key").getString(),
			)
		}
	}
}