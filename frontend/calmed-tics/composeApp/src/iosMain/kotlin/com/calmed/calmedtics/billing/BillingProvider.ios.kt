package com.calmed.calmedtics.billing

import com.calmed.calmedtics.billing.IosBillingService

private var billingService: BillingService? = null

actual fun initBilling() { /* no-op */ }

actual fun provideBillingService(): BillingService {
    return billingService ?: IosBillingService().also { billingService = it }
}