package com.calmed.calmedtics.billing

import com.calmed.calmedtics.model.raw.PaymentType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.darwin.NSObject

class IosBillingService : BillingService {
    private val _purchaseResults = MutableSharedFlow<PurchaseResult>()
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
                    paymentType = PaymentType.APPLE,
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

    override suspend fun connect() {}
    override suspend fun purchase(productId: String) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = "TriggerApplePurchase",
            `object` = null,
            userInfo = mapOf("productId" to productId)
        )
    }
    override suspend fun restore() {}
    override fun close() {
        NSNotificationCenter.defaultCenter.removeObserver(notificationObserver)
        NSNotificationCenter.defaultCenter.removeObserver(failureObserver)
    }
    override suspend fun loadProduct(productId: String): Boolean = true // Assume loaded for now
}