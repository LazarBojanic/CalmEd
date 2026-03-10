package com.calmed.calmedfrontendtourettes.payment

object StripePaymentResultBridge {
    var onResult: ((success: Boolean, paymentIntentId: String?, error: String?) -> Unit)? = null

    fun onSuccess(paymentIntentId: String) {
        onResult?.invoke(true, paymentIntentId, null)
    }

    fun onFailure(error: String) {
        onResult?.invoke(false, null, error)
    }
}
