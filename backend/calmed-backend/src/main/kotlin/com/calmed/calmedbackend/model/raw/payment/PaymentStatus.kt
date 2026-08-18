package com.calmed.calmedbackend.model.raw.payment

/**
 * Represents the lifecycle status of a payment transaction.
 */
enum class PaymentStatus {
    /** Payment was initiated but not yet completed/confirmed. */
    PENDING,

    /** Payment was successfully completed and funds captured. */
    SUCCESSFUL,

    /** Payment was refunded to the user. */
    REFUNDED,

    /** Payment access was revoked (e.g. subscription cancelled, fraud). */
    REVOKED
}
