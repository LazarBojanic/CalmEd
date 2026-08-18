package com.calmed.calmedtics.billing

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.calmed.calmedtics.model.raw.PaymentProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidBillingService(
    context: Context,
    private val activityProvider: () -> Activity?
) : BillingService, PurchasesUpdatedListener {

    private val tag = "AndroidBillingService"

    private val _purchaseResults = MutableSharedFlow<PurchaseResult>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val purchaseResults = _purchaseResults.asSharedFlow()

    private val billingClient: BillingClient =
        BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()

    private val productCache = mutableMapOf<String, ProductDetails>()
    private val connectionMutex = Mutex()

    override suspend fun connect() {
        if (billingClient.isReady) return

        connectionMutex.withLock {
            if (billingClient.isReady) return

            suspendCancellableCoroutine { cont ->
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(tag, "Billing client connected successfully.")
                            if (cont.isActive) cont.resume(Unit)
                        } else {
                            Log.e(tag, "Billing setup failed (${result.responseCode}): ${result.debugMessage}")
                            if (cont.isActive) {
                                cont.resumeWithException(
                                    IllegalStateException(
                                        "Billing setup failed (${result.responseCode}): ${result.debugMessage}"
                                    )
                                )
                            }
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        Log.w(tag, "Billing service disconnected.")
                    }
                })
            }
        }
    }

    override suspend fun purchase(productId: String) {
        connect()

        val activity = activityProvider()
            ?: throw IllegalStateException("Cannot launch billing flow without an active Activity.")

        val productDetails = productCache[productId] ?: fetchProductDetails(productId)
            ?: throw IllegalStateException("Product details not found for '$productId'. Make sure the product is configured and active in Google Play Console.")

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        val offerToken = when (productDetails.productType) {
            BillingClient.ProductType.SUBS -> {
                productDetails.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.offerToken
                    ?.takeIf { it.isNotBlank() }
            }
            BillingClient.ProductType.INAPP -> {
                productDetails.oneTimePurchaseOfferDetailsList
                    ?.firstOrNull()
                    ?.offerToken
                    ?.takeIf { it.isNotBlank() }
                    ?: productDetails.oneTimePurchaseOfferDetails
                        ?.offerToken
                        ?.takeIf { it.isNotBlank() }
            }
            else -> null
        }

        if (offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(tag, "Failed to launch billing flow: ${launchResult.debugMessage} (code ${launchResult.responseCode})")
            throw IllegalStateException(
                "Failed to launch billing flow (${launchResult.responseCode}): ${launchResult.debugMessage}"
            )
        }
    }

    override suspend fun restore() {
        connect()
        val inAppPurchases = queryOwnedPurchases(BillingClient.ProductType.INAPP)
        val subPurchases = queryOwnedPurchases(BillingClient.ProductType.SUBS)
        val restored = (inAppPurchases + subPurchases).distinctBy { it.purchaseToken }
        Log.d(tag, "Restoring purchases, found ${restored.size} owned purchase(s).")
        restored.forEach(::processPurchase)
    }

    override fun close() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    purchases.forEach(::processPurchase)
                } else {
                    Log.d(tag, "Purchases updated with OK, but purchase list was empty.")
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(tag, "User cancelled the purchase flow.")
                _purchaseResults.tryEmit(PurchaseResult.Failure("User cancelled"))
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(tag, "Item already owned. Attempting to restore purchases.")
                _purchaseResults.tryEmit(PurchaseResult.Failure("Item is already owned."))
            }
            else -> {
                Log.e(tag, "Billing error on purchases updated (${result.responseCode}): ${result.debugMessage}")
                _purchaseResults.tryEmit(
                    PurchaseResult.Failure("Billing error (${result.responseCode}): ${result.debugMessage}")
                )
            }
        }
    }

    override suspend fun loadProduct(productId: String): Boolean {
        connect()
        return fetchProductDetails(productId) != null
    }

    private suspend fun fetchProductDetails(productId: String): ProductDetails? {
        Log.d(tag, "Fetching product details for: $productId")

        suspend fun tryQuery(type: String): ProductDetails? {
            val query = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(type)
                            .build()
                    )
                )
                .build()

            return suspendCancellableCoroutine { cont ->
                billingClient.queryProductDetailsAsync(query) { result, productDetailsResult ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(
                            tag,
                            "Query for product '$productId' ($type) failed with code: ${result.responseCode}, message: ${result.debugMessage}"
                        )
                        if (cont.isActive) cont.resume(null)
                        return@queryProductDetailsAsync
                    }

                    val found = productDetailsResult.productDetailsList
                        .firstOrNull { it.productId == productId }
                    if (cont.isActive) cont.resume(found)
                }
            }
        }

        var found = tryQuery(BillingClient.ProductType.INAPP)
        if (found == null) {
            Log.d(tag, "Product '$productId' not found as INAPP, trying SUBS...")
            found = tryQuery(BillingClient.ProductType.SUBS)
        }

        if (found != null) {
            Log.d(tag, "Product '$productId' found and cached.")
            productCache[productId] = found
        } else {
            Log.w(tag, "Product '$productId' not found in any category (INAPP / SUBS).")
        }
        return found
    }

    private suspend fun queryOwnedPurchases(productType: String): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()
        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (cont.isActive) cont.resume(purchases)
                } else {
                    Log.w(
                        tag,
                        "Query owned purchases ($productType) failed with code: ${result.responseCode}, message: ${result.debugMessage}"
                    )
                    if (cont.isActive) cont.resume(emptyList())
                }
            }
        }
    }

    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            Log.d(tag, "Purchase state is ${purchase.purchaseState}, skipping acknowledgement until purchased.")
            return
        }

        val productId = purchase.products.firstOrNull() ?: ""
        _purchaseResults.tryEmit(
            PurchaseResult.Success(
                paymentProvider = PaymentProvider.GOOGLE,
                googleOrderId = purchase.orderId,
                googlePurchaseToken = purchase.purchaseToken,
                purchaseData = purchase.originalJson,
                signature = purchase.signature,
                productId = productId
            )
        )

        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(tag, "Purchase acknowledged successfully for token: ${purchase.purchaseToken}")
                } else {
                    Log.e(
                        tag,
                        "Failed to acknowledge purchase (${billingResult.responseCode}): ${billingResult.debugMessage}"
                    )
                }
            }
        }
    }
}
