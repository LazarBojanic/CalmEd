package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.propertyOrNull

data class GooglePlayConfig(
    val packageName: String,
    val publicKey: String
) {
    companion object {
        fun from(config: ApplicationConfig): GooglePlayConfig {
            val packageName = config.property("google_play.package_name").getString()
            val publicKey = config.property("google_play.public_key").getString()
            return GooglePlayConfig(
                packageName = packageName,
                publicKey = publicKey
            )
        }
    }
}
