package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.propertyOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

data class GooglePlayConfig(
    val packageName: String,
    val serviceAccountJson: String,
    val productId: String,
    val devFallbackEnabled: Boolean
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun normalizeServiceAccountJson(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return ""

            when (val parsed = runCatching { json.parseToJsonElement(trimmed) }.getOrNull()) {
                is JsonObject -> return trimmed
                is JsonPrimitive -> {
                    val content = parsed.content.trim()
                    if (runCatching { json.parseToJsonElement(content) }.isSuccess) return content
                }
                else -> {}
            }

            return runCatching {
                val body = trimmed
                    .replace("\r\n", "\\n")
                    .replace("\n", "\\n")
                    .replace("\r", "\\n")
                val decoded = json.parseToJsonElement("\"$body\"").jsonPrimitive.content
                json.parseToJsonElement(decoded)
                decoded
            }.getOrDefault(trimmed)
        }

        fun from(config: ApplicationConfig): GooglePlayConfig {
            val packageName = config.property("google_play.package_name").getString()
            val serviceAccountJson = normalizeServiceAccountJson(
                config.propertyOrNull("google_play.service_account_json")?.getString() ?: ""
            )
            val productId = config.propertyOrNull("payment.product_id")?.getString() ?: "app_access"
            val devFallbackEnabled = config.propertyOrNull("payment.dev_fallback_enabled")?.getString()?.toBoolean() ?: false
            val development = config.propertyOrNull("ktor.development")?.getString()?.toBoolean() ?: false
            check(!devFallbackEnabled || development) {
                "PAYMENT_DEV_FALLBACK_ENABLED is enabled but ktor.development is false. " +
                    "This would grant entitlements without store verification. Refusing to start."
            }
            return GooglePlayConfig(
                packageName = packageName,
                serviceAccountJson = serviceAccountJson,
                productId = productId,
                devFallbackEnabled = devFallbackEnabled
            )
        }
    }
}
