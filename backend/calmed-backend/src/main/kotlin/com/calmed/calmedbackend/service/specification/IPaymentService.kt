package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.response.PaymentSheetParamsDto
import com.calmed.calmedbackend.model.dto.response.PaymentStatusDto
import com.calmed.calmedbackend.model.raw.user.PaymentType
import java.util.UUID

interface IPaymentService {
    suspend fun paymentStatus(userId: UUID): AppResult<PaymentStatusDto>
    suspend fun createPaymentSheetParams(userId: UUID, paymentType: PaymentType): AppResult<PaymentSheetParamsDto>
    suspend fun confirmPaymentIntent(userId: UUID, paymentIntentId: String): AppResult<PaymentStatusDto>
    suspend fun skipPayment(userId: UUID): AppResult<PaymentStatusDto>
}
