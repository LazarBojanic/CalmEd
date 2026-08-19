package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.propertyOrNull

data class GooglePlayConfig(
    val packageName: String,
    val serviceAccountJson: String,
    val productId: String,
    val devFallbackEnabled: Boolean
) {
    companion object {
        fun from(config: ApplicationConfig): GooglePlayConfig {
            val packageName = config.property("google_play.package_name").getString()
            val serviceAccountJson = config.propertyOrNull("google_play.service_account_json")?.getString() ?: ""
            val productId = config.propertyOrNull("payment.product_id")?.getString() ?: "app_access"
            val devFallbackEnabled = config.propertyOrNull("payment.dev_fallback_enabled")?.getString()?.toBoolean() ?: false
            return GooglePlayConfig(
                packageName = packageName,
                serviceAccountJson = serviceAccountJson,
                productId = productId,
                devFallbackEnabled = devFallbackEnabled
            )
        }
    }
}
