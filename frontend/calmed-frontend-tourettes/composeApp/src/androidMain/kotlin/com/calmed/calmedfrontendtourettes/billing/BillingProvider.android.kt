package com.calmed.calmedfrontendtourettes.billing

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference

private lateinit var appContext: Context
private var activityRef: WeakReference<Activity>? = null
private var billingService: BillingService? = null

fun initBilling(context: Context) {
    appContext = context.applicationContext
    if (context is Activity) {
        activityRef = WeakReference(context)
    }
}


actual fun initBilling() {

}


actual fun provideBillingService(): BillingService {
    check(::appContext.isInitialized) { "initBilling(context) must be called before provideBillingService()" }
    return billingService ?: AndroidBillingService(
        context = appContext,
        activityProvider = { activityRef?.get() }
    ).also { billingService = it }
}
