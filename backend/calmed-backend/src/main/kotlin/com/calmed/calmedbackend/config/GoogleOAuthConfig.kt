package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class GoogleOAuthConfig(
	val webClientId: String
) {
	companion object {
		fun from(config: ApplicationConfig): GoogleOAuthConfig {
			return GoogleOAuthConfig(
				webClientId = config.property("oauth.google.web_client_id").getString()
			)
		}
	}
}
