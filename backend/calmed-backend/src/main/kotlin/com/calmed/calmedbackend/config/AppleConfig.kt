package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.propertyOrNull

data class AppleConfig(
    val iosBundleId: String,
    val teamId: String,
    val keyId: String,
    val clientId: String,
    val privateKeyPem: String,
    val redirectURI: String,
    val iapKeyId: String,
    val iapPrivateKeyPem: String,
    val iapIssuerId: String,
    val productId: String
) {
    companion object {
        fun from(config: ApplicationConfig): AppleConfig {
            val iosBundleId = config.propertyOrNull("oauth.apple.ios_bundle_id")?.getString() ?: ""
            val teamId = config.property("oauth.apple.team_id").getString()
            val keyId = config.property("oauth.apple.key_id").getString()
            val clientId = config.property("oauth.apple.client_id").getString()
            val privateKeyPem = config.property("oauth.apple.private_key").getString()
            val redirectURI = config.property("oauth.apple.redirect_uri").getString()
            val iapKeyId = config.property("apple.iap.key_id").getString()
            val iapPrivateKeyPem = config.property("apple.iap.private_key").getString()
            val iapIssuerId = config.property("apple.iap.issuer_id").getString()
            val productId = config.propertyOrNull("payment.product_id")?.getString() ?: "app_access"
            return AppleConfig(
                iosBundleId = iosBundleId,
                teamId = teamId,
                keyId = keyId,
                clientId = clientId,
                privateKeyPem = privateKeyPem,
                redirectURI = redirectURI,
                iapKeyId = iapKeyId,
                iapPrivateKeyPem = iapPrivateKeyPem,
                iapIssuerId = iapIssuerId,
                productId = productId
            )
        }
    }
}
