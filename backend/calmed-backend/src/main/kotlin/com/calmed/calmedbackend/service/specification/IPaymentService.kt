package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.CreateCheckoutSessionDto
import com.calmed.calmedbackend.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedbackend.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedbackend.model.dto.response.CheckoutSessionResponseDto
import com.calmed.calmedbackend.model.dto.response.PaymentStatusDto
import com.calmed.calmedbackend.model.raw.user.PaymentType
import java.util.UUID

interface IPaymentService {
    suspend fun paymentStatus(userId: UUID): AppResult<PaymentStatusDto>
    suspend fun createCheckoutSession(userId: UUID, dto: CreateCheckoutSessionDto): AppResult<CheckoutSessionResponseDto>
    suspend fun handleStripeWebhook(payload: String, sigHeader: String): AppResult<Unit>
    suspend fun skipPayment(userId: UUID): AppResult<PaymentStatusDto>
    suspend fun verifyApplePurchase(userId: UUID, dto: VerifyAppleReceiptDto): AppResult<PaymentStatusDto>
    suspend fun verifyGooglePurchase(userId: UUID, dto: VerifyGoogleReceiptDto): AppResult<PaymentStatusDto>
    suspend fun verifyStripeSession(userId: UUID, sessionId: String): AppResult<PaymentStatusDto>
}
