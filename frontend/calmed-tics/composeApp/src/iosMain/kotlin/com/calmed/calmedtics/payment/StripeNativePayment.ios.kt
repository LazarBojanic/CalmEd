package com.calmed.calmedtics.payment

import com.calmed.calmedtics.model.dto.response.PaymentSheetParamsDto
import platform.Foundation.NSNotificationCenter

actual fun launchStripePaymentSheet(params: PaymentSheetParamsDto) {
    val payload: Map<Any?, *> = mapOf(
        "paymentIntentId" to params.paymentIntentId,
        "paymentIntentClientSecret" to params.paymentIntentClientSecret,
        "customerId" to params.customerId,
        "customerEphemeralKeySecret" to params.customerEphemeralKeySecret,
        "publishableKey" to params.publishableKey,
        "merchantDisplayName" to params.merchantDisplayName,
        "merchantCountryCode" to params.merchantCountryCode,
        "appleMerchantId" to params.appleMerchantId
    )
    NSNotificationCenter.defaultCenter.postNotificationName(
        "StartStripePayment",
        null,
        payload
    )
}
