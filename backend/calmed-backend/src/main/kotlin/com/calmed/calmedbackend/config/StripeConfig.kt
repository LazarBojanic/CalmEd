package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class StripeConfig(
    val secretKey: String,
    val publishableKey: String,
    val webhookSecret: String,
    val amount: String,
    val currency: String
) {
    val amountCents: Long
        get() = (amount.toDouble() * 100).toLong()

    companion object {
        fun from(config: ApplicationConfig): StripeConfig {
            return StripeConfig(
                secretKey = config.property("stripe.secret_key").getString(),
                publishableKey = config.property("stripe.publishable_key").getString(),
                webhookSecret = config.property("stripe.webhook_secret").getString(),
                amount = config.property("stripe.amount").getString(),
                currency = config.property("stripe.currency").getString()
            )
        }
    }
}
