package com.calmed.calmedtics.payment

import com.calmed.calmedtics.MainActivity
import com.calmed.calmedtics.model.dto.response.PaymentSheetParamsDto

actual fun launchStripePaymentSheet(params: PaymentSheetParamsDto) {
    MainActivity.stripePaymentStarter?.invoke(params)
        ?: StripePaymentResultBridge.onFailure("Stripe payment is not initialized.")
}
