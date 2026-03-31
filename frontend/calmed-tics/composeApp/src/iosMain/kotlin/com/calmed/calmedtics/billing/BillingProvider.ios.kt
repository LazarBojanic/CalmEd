package com.calmed.calmedtics.billing

actual fun initBilling() { /* no-op */ }

actual fun provideBillingService(): BillingService = IosBillingService()