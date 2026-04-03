package com.calmed.calmedtics.billing

import com.calmed.calmedtics.BuildConfig

private var billingService: BillingService? = null

actual fun initBilling() { /* no-op */ }

actual fun provideBillingService(): BillingService {
    if (BuildConfig.useMockBilling) {
        return billingService ?: MockBillingService().also { billingService = it }
    }
    return billingService ?: IosBillingService().also { billingService = it }
}