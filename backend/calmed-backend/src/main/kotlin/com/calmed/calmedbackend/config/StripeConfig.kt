package com.calmed.calmedbackend.config

import io.ktor.server.config.ApplicationConfig

data class StripeConfig(
    val secretKey: String,
    val publishableKey: String,
    val webhookSecret: String,
    val amountCents: Long,
    val currency: String
) {
    companion object {
        fun from(config: ApplicationConfig): StripeConfig {
            return StripeConfig(
                secretKey = config.property("stripe.secret_key").getString(),
                publishableKey = config.property("stripe.publishable_key").getString(),
                webhookSecret = config.property("stripe.webhook_secret").getString(),
                amountCents = config.property("stripe.amount_cents").getString().toLong(),
                currency = config.property("stripe.currency").getString()
            )
        }
    }
}
