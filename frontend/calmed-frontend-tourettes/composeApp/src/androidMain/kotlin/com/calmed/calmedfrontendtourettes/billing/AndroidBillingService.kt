package com.calmed.calmedfrontendtourettes.billing

import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


private val productCache = mutableMapOf<String, ProductDetails>()

class AndroidBillingService(

    private val context: Context
) : BillingService, PurchasesUpdatedListener {

    private val billingClient: BillingClient =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    override suspend fun connect() {
        if (billingClient.isReady) return

        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    cont.resume(Unit)
                }
                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    override suspend fun purchase(productId: String) {

        error("Not implemented yet")
    }

    override suspend fun restore() {

    }

    override fun close() {
        if (billingClient.isReady) billingClient.endConnection()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {

    }
    override suspend fun loadProduct(productId: String): Boolean {
        val query = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP) // one-time
                        .build()
                )
            )
            .build()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(query) { result, productDetailsList ->
                val ok = result.responseCode == BillingClient.BillingResponseCode.OK
                val found = productDetailsList.firstOrNull()

                if (ok && found != null) {
                    productCache[productId] = found
                    cont.resume(true)
                } else {
                    cont.resume(false)
                }
            }
        }
    }
}