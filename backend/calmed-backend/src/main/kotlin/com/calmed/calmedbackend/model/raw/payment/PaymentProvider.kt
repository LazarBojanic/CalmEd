package com.calmed.calmedbackend.model.raw.payment

/**
 * Identifies the payment provider through which a transaction was processed.
 */
enum class PaymentProvider {
    /** Google Play Store billing (Android). */
    GOOGLE,

    /** Apple App Store billing (iOS). */
    APPLE,

    /** PayPal checkout. */
    PAYPAL,

    /** Stripe checkout/payment. */
    STRIPE,

    /** Development-only payment bypass. */
    SKIP
}
