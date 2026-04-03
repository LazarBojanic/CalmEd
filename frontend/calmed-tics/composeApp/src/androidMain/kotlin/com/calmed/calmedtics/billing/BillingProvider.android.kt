package com.calmed.calmedtics.billing

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference
import calmedtics.composeApp.BuildConfig

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
    if (BuildConfig.useMockBilling) {
        return billingService ?: MockBillingService().also { billingService = it }
    }
    return billingService ?: AndroidBillingService(
        context = appContext,
        activityProvider = { activityRef?.get() }
    ).also { billingService = it }
}
