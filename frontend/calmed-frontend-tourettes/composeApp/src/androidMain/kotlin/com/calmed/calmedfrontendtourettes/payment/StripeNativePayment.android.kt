package com.calmed.calmedfrontendtourettes.payment

import com.calmed.calmedfrontendtourettes.MainActivity
import com.calmed.calmedfrontendtourettes.model.dto.response.PaymentSheetParamsDto

actual fun launchStripePaymentSheet(params: PaymentSheetParamsDto) {
    MainActivity.stripePaymentStarter?.invoke(params)
        ?: StripePaymentResultBridge.onFailure("Stripe payment is not initialized.")
}
