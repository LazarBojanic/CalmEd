package com.calmed.calmedtics.billing

import com.calmed.calmedtics.model.raw.PaymentProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.darwin.NSObject

class IosBillingService : BillingService {
    private val _purchaseResults = MutableSharedFlow<PurchaseResult>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val purchaseResults = _purchaseResults.asSharedFlow()

    private val notificationObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = "OnApplePurchaseSuccess",
        `object` = null,
        queue = NSOperationQueue.mainQueue
    ) { notification ->
        val userInfo = notification?.userInfo
        val transactionId = userInfo?.get("transactionId") as? String
        val productId = userInfo?.get("productId") as? String
        
        if (transactionId != null && productId != null) {
            _purchaseResults.tryEmit(
                PurchaseResult.Success(
                    paymentProvider = PaymentProvider.APPLE,
                    appleTransactionId = transactionId,
                    productId = productId
                )
            )
        }
    }

    private val failureObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = "OnApplePurchaseFailure",
        `object` = null,
        queue = NSOperationQueue.mainQueue
    ) { notification ->
        val error = notification?.userInfo?.get("error") as? String ?: "Unknown error"
        _purchaseResults.tryEmit(PurchaseResult.Failure(error))
    }

    private val restoreCompleteObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = "OnAppleRestoreComplete",
        `object` = null,
        queue = NSOperationQueue.mainQueue
    ) { notification ->
        val count = (notification?.userInfo?.get("count") as? NSNumber)?.intValue ?: 0
        if (count <= 0) {
            _purchaseResults.tryEmit(PurchaseResult.NothingToRestore)
        }
    }

    override suspend fun connect() {}
    override suspend fun purchase(productId: String, obfuscatedAccountId: String?) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = "TriggerApplePurchase",
            `object` = null,
            userInfo = mapOf("productId" to productId)
        )
    }
    override suspend fun restore() {
        // Ask the native StoreKit layer to sync (restore) owned purchases. New/updated
        // transactions flow back through the "OnApplePurchaseSuccess" notification above.
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = "TriggerAppleRestore",
            `object` = null,
            userInfo = null
        )
    }
    override fun close() {
        NSNotificationCenter.defaultCenter.removeObserver(notificationObserver)
        NSNotificationCenter.defaultCenter.removeObserver(failureObserver)
        NSNotificationCenter.defaultCenter.removeObserver(restoreCompleteObserver)
    }
    override suspend fun loadProduct(productId: String): Boolean = true // Assume loaded for now
}