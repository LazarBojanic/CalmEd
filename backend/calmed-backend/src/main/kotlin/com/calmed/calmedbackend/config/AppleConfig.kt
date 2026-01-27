package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.propertyOrNull

data class AppleConfig(
    val teamId: String,
    val keyId: String,
    val clientId: String,
    val privateKeyPem: String,
    val redirectURI: String
) {
    companion object {
        fun from(config: ApplicationConfig): AppleConfig {
            val teamId = config.property("oauth.apple.team_id").getString()
            val keyId = config.property("oauth.apple.key_id").getString()
            val clientId = config.property("oauth.apple.client_id").getString()
            val privateKeyPemRaw = config.property("oauth.apple.private_key").getString()

            val privateKeyPem = privateKeyPemRaw.replace("\\n", "\n")
            val redirectURI = config.property("oauth.apple.redirect_uri").getString()
            return AppleConfig(
                teamId = teamId,
                keyId = keyId,
                clientId = clientId,
                privateKeyPem = privateKeyPem,
                redirectURI = redirectURI
            )
        }
    }
}
