package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.config.StripeConfig
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.CreateCheckoutSessionDto
import com.calmed.calmedbackend.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedbackend.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedbackend.model.dto.response.CheckoutSessionResponseDto
import com.calmed.calmedbackend.model.dto.response.PaymentStatusDto
import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.service.specification.IPaymentService
import com.calmed.calmedbackend.service.specification.IUserService
import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import io.ktor.http.HttpStatusCode
import java.util.UUID

class PaymentService(
    private val userService: IUserService,
    private val stripeConfig: StripeConfig
) : IPaymentService {

    init {
        Stripe.apiKey = stripeConfig.secretKey
    }

    override suspend fun paymentStatus(userId: UUID): AppResult<PaymentStatusDto> {
        return when (val userResult = userService.getById(userId)) {
            is AppResult.Success -> AppResult.Success(
                PaymentStatusDto(
                    isPaid = userResult.data.isPaid,
                    paymentType = userResult.data.paymentType,
                    amountCents = stripeConfig.amountCents,
                    currency = stripeConfig.currency
                )
            )

            is AppResult.Failure -> AppResult.Failure(userResult.httpStatusCode, userResult.message)
        }
    }

    override suspend fun createCheckoutSession(
        userId: UUID,
        dto: CreateCheckoutSessionDto
    ): AppResult<CheckoutSessionResponseDto> {
        return try {
            val params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(dto.successUrl)
                .setCancelUrl(dto.cancelUrl)
                .setClientReferenceId(userId.toString())
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(stripeConfig.currency)
                                .setUnitAmount(stripeConfig.amountCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("CalmEd Program")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()

            val session = Session.create(params)
            AppResult.Success(CheckoutSessionResponseDto(session.url))
        } catch (e: Exception) {
            AppResult.Failure(HttpStatusCode.InternalServerError, e.message ?: "Failed to create checkout session")
        }
    }

    override suspend fun handleStripeWebhook(payload: String, sigHeader: String): AppResult<Unit> {
        println("[DEBUG_LOG] PaymentService: handleStripeWebhook called. Payload: ${payload.take(100)}...")
        return try {
            val event = try {
                Webhook.constructEvent(
                    payload, sigHeader, stripeConfig.webhookSecret
                )
            } catch (e: Exception) {
                println("[DEBUG_LOG] PaymentService: Webhook signature verification FAILED: ${e.message}")
                return AppResult.Failure(HttpStatusCode.BadRequest, "Signature verification failed")
            }
            
            println("[DEBUG_LOG] PaymentService: Webhook event constructed. ID: ${event.id}, Type: ${event.type}")

            if (event.type == "checkout.session.completed") {
                val session = event.dataObjectDeserializer.`object`.get() as Session
                val userIdStr = session.clientReferenceId
                println("[DEBUG_LOG] PaymentService: checkout.session.completed. ClientReferenceId (UserId): $userIdStr, Customer: ${session.customer}")
                
                if (userIdStr != null) {
                    val userId = try { UUID.fromString(userIdStr) } catch(e: Exception) { null }
                    if (userId == null) {
                        println("[DEBUG_LOG] PaymentService: CRITICAL: clientReferenceId $userIdStr is not a valid UUID")
                        return AppResult.Failure(HttpStatusCode.BadRequest, "Invalid clientReferenceId")
                    }

                    val customerId = session.customer
                    println("[DEBUG_LOG] PaymentService: Updating payment status for user $userId")
                    val result = userService.setPaymentStatus(
                        id = userId,
                        isPaid = true,
                        paymentType = PaymentType.CARD,
                        stripeCustomerId = customerId
                    )
                    when (result) {
                        is AppResult.Success -> {
                            println("[DEBUG_LOG] PaymentService: DB Update Success for user $userId. isPaid in returned data: ${result.data.isPaid}")
                        }
                        is AppResult.Failure -> {
                            println("[DEBUG_LOG] PaymentService: DB Update FAILURE for user $userId: ${result.message}")
                        }
                    }
                } else {
                    println("[DEBUG_LOG] PaymentService: CRITICAL: clientReferenceId is null in checkout.session.completed")
                }
            } else {
                println("[DEBUG_LOG] PaymentService: Received unhandled event type: ${event.type}")
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            println("[DEBUG_LOG] PaymentService: Unexpected error during webhook processing: ${e.message}")
            e.printStackTrace()
            AppResult.Failure(HttpStatusCode.InternalServerError, "Internal error")
        }
    }

    override suspend fun skipPayment(userId: UUID): AppResult<PaymentStatusDto> {
        return when (val res = userService.setPaymentStatus(userId, true, PaymentType.CARD)) {
            is AppResult.Success -> AppResult.Success(
                PaymentStatusDto(
                    isPaid = true,
                    paymentType = PaymentType.CARD,
                    currency = stripeConfig.currency,
                    amountCents = stripeConfig.amountCents
                )
            )

            is AppResult.Failure -> AppResult.Failure(res.httpStatusCode, res.message)
        }
    }

    override suspend fun verifyApplePurchase(userId: UUID, dto: VerifyAppleReceiptDto): AppResult<PaymentStatusDto> {
        val updateResult = userService.setPaymentStatus(
            id = userId,
            isPaid = true,
            paymentType = PaymentType.APPLE,
            appleOriginalTransactionId = dto.transactionId
        )
        return when (updateResult) {
            is AppResult.Success -> AppResult.Success(
                PaymentStatusDto(
                    isPaid = true,
                    paymentType = updateResult.data.paymentType,
                    amountCents = stripeConfig.amountCents,
                    currency = stripeConfig.currency
                )
            )
            is AppResult.Failure -> AppResult.Failure(updateResult.httpStatusCode, updateResult.message)
        }
    }

    override suspend fun verifyGooglePurchase(userId: UUID, dto: VerifyGoogleReceiptDto): AppResult<PaymentStatusDto> {
        val updateResult = userService.setPaymentStatus(
            id = userId,
            isPaid = true,
            paymentType = PaymentType.GOOGLE,
            googleOrderId = dto.orderId
        )
        return when (updateResult) {
            is AppResult.Success -> AppResult.Success(
                PaymentStatusDto(
                    isPaid = true,
                    paymentType = updateResult.data.paymentType,
                    amountCents = stripeConfig.amountCents,
                    currency = stripeConfig.currency
                )
            )
            is AppResult.Failure -> AppResult.Failure(updateResult.httpStatusCode, updateResult.message)
        }
    }

    override suspend fun verifyStripeSession(userId: UUID, sessionId: String): AppResult<PaymentStatusDto> {
        return try {
            val session = Session.retrieve(sessionId)
            if (session.clientReferenceId == userId.toString() && session.paymentStatus == "paid") {
                val updateResult = userService.setPaymentStatus(
                    id = userId,
                    isPaid = true,
                    paymentType = PaymentType.CARD,
                    stripeCustomerId = session.customer
                )
                when (updateResult) {
                    is AppResult.Success -> AppResult.Success(
                        PaymentStatusDto(
                            isPaid = true,
                            paymentType = updateResult.data.paymentType,
                            amountCents = stripeConfig.amountCents,
                            currency = stripeConfig.currency
                        )
                    )
                    is AppResult.Failure -> AppResult.Failure(updateResult.httpStatusCode, updateResult.message)
                }
            } else {
                AppResult.Failure(HttpStatusCode.BadRequest, "Session not paid or user mismatch")
            }
        } catch (e: Exception) {
            AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to verify session: ${e.message}")
        }
    }
}
