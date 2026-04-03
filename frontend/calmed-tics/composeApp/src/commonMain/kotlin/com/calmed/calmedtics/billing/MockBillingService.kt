package com.calmed.calmedtics.billing

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.calmed.calmedtics.model.raw.PaymentType
import com.calmed.calmedtics.getPlatform

class MockBillingService : BillingService {
    private val _purchaseResults = MutableSharedFlow<PurchaseResult>()
    override val purchaseResults = _purchaseResults.asSharedFlow()

    override suspend fun connect() {
        delay(500)
        println("[DEBUG_LOG] MockBillingService connected")
    }

    override suspend fun loadProduct(productId: String): Boolean {
        delay(300)
        println("[DEBUG_LOG] MockBillingService loaded product: $productId")
        return true
    }

    override suspend fun purchase(productId: String) {
        println("[DEBUG_LOG] MockBillingService starting purchase for: $productId")
        delay(1500) // Simulate UI delay
        
        val platformName = getPlatform().name
        val isAndroid = platformName.startsWith("Android", ignoreCase = true)
        
        val result = if (isAndroid) {
            PurchaseResult.Success(
                paymentType = PaymentType.GOOGLE,
                googleOrderId = "mock_google_order_${System.currentTimeMillis()}",
                googlePurchaseToken = "mock_google_token",
                productId = productId
            )
        } else {
            PurchaseResult.Success(
                paymentType = PaymentType.APPLE,
                appleTransactionId = "mock_apple_trans_${System.currentTimeMillis()}",
                productId = productId
            )
        }
        
        _purchaseResults.emit(result)
        println("[DEBUG_LOG] MockBillingService emitted success for: $productId")
    }

    override suspend fun restore() {
        delay(1000)
        println("[DEBUG_LOG] MockBillingService restored purchases")
    }

    override fun close() {
        println("[DEBUG_LOG] MockBillingService closed")
    }
}
