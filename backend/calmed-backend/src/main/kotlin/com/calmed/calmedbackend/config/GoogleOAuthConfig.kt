package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class GoogleOAuthConfig(
	val webClientId: String,
	val iosClientId: String? = null,
	val androidClientId: String? = null
) {
	companion object {
		fun from(config: ApplicationConfig): GoogleOAuthConfig {
			return GoogleOAuthConfig(
				webClientId = config.property("oauth.google.web_client_id").getString(),
				iosClientId = config.property("oauth.google.ios_client_id").getString(),
				androidClientId = config.property("oauth.google.android_client_id").getString()
			)
		}
	}
}
