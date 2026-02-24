package com.calmed.calmedfrontendtourettes.billing

actual fun initBilling() { /* no-op */ }

actual fun provideBillingService(): BillingService = IosBillingService()