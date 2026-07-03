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
import com.calmed.calmedbackend.config.PayPalConfig
import com.calmed.calmedbackend.model.dto.request.CapturePayPalOrderDto
import com.calmed.calmedbackend.model.dto.response.PayPalOrderResponseDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64
import kotlinx.serialization.json.jsonArray

class PaymentService(
    private val userService: IUserService,
    private val stripeConfig: StripeConfig,
    private val paypalConfig: PayPalConfig
) : IPaymentService {

    init {
        Stripe.apiKey = stripeConfig.secretKey
    }

    private val paypalHttpClient = HttpClient.newHttpClient()

    private val paypalJson = Json {
        ignoreUnknownKeys = true
    }

    private fun getPayPalAccessToken(): String {
        val credentials = "${paypalConfig.clientId}:${paypalConfig.clientSecret}"
        val encodedCredentials = Base64.getEncoder()
            .encodeToString(credentials.toByteArray())

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${paypalConfig.baseUrl}/v1/oauth2/token"))
            .header("Authorization", "Basic $encodedCredentials")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build()

        val response = paypalHttpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Failed to get PayPal access token: ${response.body()}")
        }

        val json = paypalJson.parseToJsonElement(response.body()).jsonObject

        return json["access_token"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("PayPal access_token missing")
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
    override suspend fun createPayPalOrder(userId: UUID): AppResult<PayPalOrderResponseDto> {
        return try {
            val accessToken = getPayPalAccessToken()

            val requestBody = """
            {
              "intent": "CAPTURE",
              "purchase_units": [
                {
                  "reference_id": "$userId",
                  "description": "CalmEd Program",
                  "amount": {
                    "currency_code": "${paypalConfig.currency}",
                    "value": "${paypalConfig.amount}"
                  }
                }
              ]
            }
        """.trimIndent()

            val request = HttpRequest.newBuilder()
                .uri(URI.create("${paypalConfig.baseUrl}/v2/checkout/orders"))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = paypalHttpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            if (response.statusCode() !in 200..299) {
                return AppResult.Failure(
                    HttpStatusCode.BadRequest,
                    "Failed to create PayPal order: ${response.body()}"
                )
            }

            val json = paypalJson.parseToJsonElement(response.body()).jsonObject

            val orderId = json["id"]?.jsonPrimitive?.content
                ?: return AppResult.Failure(
                    HttpStatusCode.BadRequest,
                    "PayPal order id missing"
                )

            AppResult.Success(
                PayPalOrderResponseDto(orderId = orderId)
            )

        } catch (e: Exception) {
            AppResult.Failure(
                HttpStatusCode.InternalServerError,
                "PayPal create order failed: ${e.message}"
            )
        }
    }

    override suspend fun capturePayPalOrder(
        userId: UUID,
        dto: CapturePayPalOrderDto
    ): AppResult<PaymentStatusDto> {
        return try {
            val accessToken = getPayPalAccessToken()

            val request = HttpRequest.newBuilder()
                .uri(URI.create("${paypalConfig.baseUrl}/v2/checkout/orders/${dto.orderId}/capture"))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build()

            val response = paypalHttpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            if (response.statusCode() !in 200..299) {
                return AppResult.Failure(
                    HttpStatusCode.BadRequest,
                    "Failed to capture PayPal order: ${response.body()}"
                )
            }

            val json = paypalJson.parseToJsonElement(response.body()).jsonObject

            val status = json["status"]?.jsonPrimitive?.content

            if (status != "COMPLETED") {
                return AppResult.Failure(
                    HttpStatusCode.BadRequest,
                    "PayPal payment is not completed"
                )
            }

            val referenceId = json["purchase_units"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("reference_id")
                ?.jsonPrimitive
                ?.content

            if (referenceId != userId.toString()) {
                return AppResult.Failure(
                    HttpStatusCode.BadRequest,
                    "PayPal order does not belong to this user"
                )
            }

            val updateResult = userService.setPaymentStatus(
                id = userId,
                isPaid = true,
                paymentType = PaymentType.PAYPAL,
                stripeCustomerId = null
            )

            when (updateResult) {
                is AppResult.Success -> {
                    paymentStatus(userId)
                }

                is AppResult.Failure -> {
                    AppResult.Failure(
                        updateResult.httpStatusCode,
                        updateResult.message
                    )
                }
            }

        } catch (e: Exception) {
            AppResult.Failure(
                HttpStatusCode.InternalServerError,
                "PayPal capture order failed: ${e.message}"
            )
        }
    }
}
