package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.propertyOrNull

data class StripeConfig(
    val secretKey: String,
    val publishableKey: String,
    val webhookSecret: String?,
    val amountCents: Long,
    val currency: String,
    val merchantDisplayName: String,
    val merchantCountryCode: String,
    val appleMerchantId: String?,
    val apiVersion: String
) {
    companion object {
        fun from(config: ApplicationConfig): StripeConfig {
            return StripeConfig(
                secretKey = config.property("stripe.secret_key").getString(),
                publishableKey = config.property("stripe.publishable_key").getString(),
                webhookSecret = config.propertyOrNull("stripe.webhook_secret")?.getString()?.takeIf { it.isNotBlank() },
                amountCents = config.property("stripe.amount_cents").getString().toLong(),
                currency = config.property("stripe.currency").getString(),
                merchantDisplayName = config.property("stripe.merchant_display_name").getString(),
                merchantCountryCode = config.property("stripe.merchant_country_code").getString(),
                appleMerchantId = config.propertyOrNull("stripe.apple_merchant_id")?.getString()?.takeIf { it.isNotBlank() },
                apiVersion = config.property("stripe.api_version").getString()
            )
        }
    }
}
