package com.calmed.calmedfrontendtourettes.payment

import com.calmed.calmedfrontendtourettes.model.dto.response.PaymentSheetParamsDto

expect fun launchStripePaymentSheet(
    params: PaymentSheetParamsDto
)
