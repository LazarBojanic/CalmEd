package com.calmed.calmedfrontendtourettes.billing

import android.content.Context

private lateinit var appContext: Context

fun initBilling(context: Context) {
    appContext = context.applicationContext
}


actual fun initBilling() {

}


actual fun provideBillingService(): BillingService {
    check(::appContext.isInitialized) { "initBilling(context) must be called before provideBillingService()" }
    return AndroidBillingService(appContext)
}