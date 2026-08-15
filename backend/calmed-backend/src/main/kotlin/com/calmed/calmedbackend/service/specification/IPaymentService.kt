package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.*
import com.calmed.calmedbackend.model.dto.response.*
import com.calmed.calmedbackend.model.raw.payment.Payment
import java.util.UUID

interface IPaymentService {
    suspend fun paymentStatus(userId: UUID): AppResult<PaymentStatusDto>
    suspend fun createCheckoutSession(userId: UUID, dto: CreateCheckoutSessionDto): AppResult<CheckoutSessionResponseDto>
    suspend fun handleStripeWebhook(payload: String, sigHeader: String): AppResult<Unit>
    suspend fun skipPayment(userId: UUID): AppResult<PaymentStatusDto>
    suspend fun verifyApplePurchase(userId: UUID, dto: VerifyAppleReceiptDto): AppResult<PaymentStatusDto>
    suspend fun verifyGooglePurchase(userId: UUID, dto: VerifyGoogleReceiptDto): AppResult<PaymentStatusDto>
    suspend fun verifyStripeSession(userId: UUID, sessionId: String): AppResult<PaymentStatusDto>
    suspend fun createPayPalOrder(userId: UUID): AppResult<PayPalOrderResponseDto>
    suspend fun capturePayPalOrder(userId: UUID, dto: CapturePayPalOrderDto): AppResult<PaymentStatusDto>
    suspend fun getAll(): AppResult<List<Payment>>
    suspend fun getById(id: UUID): AppResult<Payment>
    suspend fun getByUserId(userId: UUID): AppResult<List<Payment>>
}
