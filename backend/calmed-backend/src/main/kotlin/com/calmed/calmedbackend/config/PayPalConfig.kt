package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class PayPalConfig(
    val clientId: String,
    val clientSecret: String,
    val baseUrl: String,
    val amount: String,
    val currency: String
) {
    companion object {
        fun from(config: ApplicationConfig): PayPalConfig {
            return PayPalConfig(
                clientId = config.property("paypal.client_id").getString(),
                clientSecret = config.property("paypal.client_secret").getString(),
                baseUrl = config.property("paypal.base_url").getString(),
                amount = config.property("paypal.amount").getString(),
                currency = config.property("paypal.currency").getString()
            )
        }
    }
}