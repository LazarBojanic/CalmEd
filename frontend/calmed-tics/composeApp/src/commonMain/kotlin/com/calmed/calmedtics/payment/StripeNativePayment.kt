package com.calmed.calmedtics.payment

import com.calmed.calmedtics.model.dto.response.PaymentSheetParamsDto

expect fun launchStripePaymentSheet(
    params: PaymentSheetParamsDto
)
