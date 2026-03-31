package com.calmed.calmedtics.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidBillingService(
    context: Context,
    private val activityProvider: () -> Activity?
) : BillingService, PurchasesUpdatedListener {

    private val billingClient: BillingClient =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()

    private val productCache = mutableMapOf<String, ProductDetails>()

    override suspend fun connect() {
        if (billingClient.isReady) return

        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        cont.resume(Unit)
                    } else {
                        cont.resumeWithException(
                            IllegalStateException(
                                "Billing setup failed (${result.responseCode}): ${result.debugMessage}"
                            )
                        )
                    }
                }
                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    override suspend fun purchase(productId: String) {
        connect()

        val activity = activityProvider()
            ?: throw IllegalStateException("Cannot launch billing flow without an active Activity.")

        val productDetails = productCache[productId] ?: fetchProductDetails(productId)
        ?: throw IllegalStateException("Product details not found for '$productId'.")

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        // For one-time products with multiple offers, pass an offer token for the selected offer.
        val oneTimeOfferToken = productDetails.oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?.offerToken
            ?.takeIf { it.isNotBlank() }
            ?: productDetails.oneTimePurchaseOfferDetails
                ?.offerToken
                ?.takeIf { it.isNotBlank() }

        if (oneTimeOfferToken != null) {
            productDetailsParamsBuilder.setOfferToken(oneTimeOfferToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw IllegalStateException(
                "Failed to launch billing flow (${launchResult.responseCode}): ${launchResult.debugMessage}"
            )
        }
    }

    override suspend fun restore() {
        connect()
        val restored = buildList {
            addAll(queryOwnedPurchases(BillingClient.ProductType.INAPP))
            addAll(queryOwnedPurchases(BillingClient.ProductType.SUBS))
        }.distinctBy { it.purchaseToken }
        restored.forEach(::processPurchase)
    }

    override fun close() {
        if (billingClient.isReady) billingClient.endConnection()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases.isNullOrEmpty()) {
            return
        }
        purchases.forEach(::processPurchase)
    }

    override suspend fun loadProduct(productId: String): Boolean {
        connect()
        return fetchProductDetails(productId) != null
    }

    private suspend fun fetchProductDetails(productId: String): ProductDetails? {
        val query = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(query) { result, productDetailsResult ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    cont.resume(null)
                    return@queryProductDetailsAsync
                }

                val found = productDetailsResult.productDetailsList
                    .firstOrNull { it.productId == productId }
                if (found != null) {
                    productCache[productId] = found
                }
                cont.resume(found)
            }
        }
    }

    private suspend fun queryOwnedPurchases(productType: String): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()
        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    cont.resume(purchases)
                } else {
                    cont.resume(emptyList())
                }
            }
        }
    }

    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (purchase.isAcknowledged) return

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { /* no-op */ }
    }
}
