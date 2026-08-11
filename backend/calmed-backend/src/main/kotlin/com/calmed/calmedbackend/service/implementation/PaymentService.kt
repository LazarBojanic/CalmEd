package com.calmed.calmedbackend.service.implementation
 
import com.calmed.calmedbackend.config.StripeConfig
import com.calmed.calmedbackend.config.PayPalConfig
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.*
import com.calmed.calmedbackend.model.dto.response.*
import com.calmed.calmedbackend.model.raw.payment.Payment
import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.repository.specification.IPaymentRepository
import com.calmed.calmedbackend.service.specification.IPaymentService
import com.calmed.calmedbackend.service.specification.IUserService
import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import io.ktor.http.HttpStatusCode
import java.util.UUID
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
    private val paymentRepository: IPaymentRepository,
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
            ?: throw IllegalStateException("PayPal response missing access_token")
    }

    override suspend fun paymentStatus(userId: UUID): AppResult<PaymentStatusDto> {
        val userRes = userService.getById(userId)
        return when (userRes) {
            is AppResult.Success -> {
                val user = userRes.data
                AppResult.Success(
                    PaymentStatusDto(
                        isPaid = user.isPaid,
                        amount = stripeConfig.amount,
                        currency = stripeConfig.currency
                    )
                )
            }
            is AppResult.Failure -> AppResult.Failure(userRes.httpStatusCode, userRes.message)
        }
    }

    override suspend fun createCheckoutSession(userId: UUID, dto: CreateCheckoutSessionDto): AppResult<CheckoutSessionResponseDto> {
        return try {
            val params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(dto.successUrl)
                .setCancelUrl(dto.cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(stripeConfig.currency.lowercase())
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
                .putMetadata("userId", userId.toString())
                .build()

            val session = Session.create(params)
            AppResult.Success(CheckoutSessionResponseDto(session.id, session.url))
        } catch (e: Exception) {
            AppResult.Failure(HttpStatusCode.InternalServerError, e.message ?: "Stripe error")
        }
    }

	override suspend fun handleStripeWebhook(payload: String, sigHeader: String): AppResult<Unit> {
        return try {
            val event = Webhook.constructEvent(payload, sigHeader, stripeConfig.webhookSecret)
            if (event.type == "checkout.session.completed") {
                val session = event.dataObjectDeserializer.getObject().get() as Session
                val userIdStr = session.metadata["userId"]
                if (userIdStr != null) {
                    val userId = UUID.fromString(userIdStr)
                    
                    // 1. Create Payment Record
                    paymentRepository.create(
                        Payment.createNew(
                            userId = userId,
                            paymentType = PaymentType.STRIPE,
                            stripeCustomerId = session.customer,
                            successful = true
                        )
                    )
                    
                    // 2. Update User Status
                    userService.setPaymentStatus(
                        id = userId,
                        isPaid = true,
                        stripeCustomerId = session.customer
                    )
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(HttpStatusCode.BadRequest, "Webhook error: ${e.message}")
        }
    }

    override suspend fun skipPayment(userId: UUID): AppResult<PaymentStatusDto> {
        // 1. Create Payment Record
        paymentRepository.create(
            Payment.createNew(
                userId = userId,
                paymentType = PaymentType.SKIP,
                successful = true
            )
        )
        
        // 2. Update User Status
        userService.setPaymentStatus(
            id = userId,
            isPaid = true
        )
        return paymentStatus(userId)
    }

    override suspend fun verifyApplePurchase(userId: UUID, dto: VerifyAppleReceiptDto): AppResult<PaymentStatusDto> {
        // 1. Create Payment Record
        paymentRepository.create(
            Payment.createNew(
                userId = userId,
                paymentType = PaymentType.APPLE,
                appleOriginalTransactionId = dto.transactionId,
                successful = true
            )
        )
        
        // 2. Update User Status
        userService.setPaymentStatus(
            id = userId,
            isPaid = true
        )
        return paymentStatus(userId)
    }

    override suspend fun verifyGooglePurchase(userId: UUID, dto: VerifyGoogleReceiptDto): AppResult<PaymentStatusDto> {
        // 1. Create Payment Record
        paymentRepository.create(
            Payment.createNew(
                userId = userId,
                paymentType = PaymentType.GOOGLE,
                googleOrderId = dto.purchaseToken,
                successful = true
            )
        )
        
        // 2. Update User Status
        userService.setPaymentStatus(
            id = userId,
            isPaid = true
        )
        return paymentStatus(userId)
    }

    override suspend fun verifyStripeSession(userId: UUID, sessionId: String): AppResult<PaymentStatusDto> {
        return try {
            val session = Session.retrieve(sessionId)
            val isPaid = session.paymentStatus == "paid"
            
            // 1. Create Payment Record
            paymentRepository.create(
                Payment.createNew(
                    userId = userId,
                    paymentType = PaymentType.STRIPE,
                    stripeCustomerId = session.customer,
                    successful = isPaid
                )
            )
            
            // 2. Update User Status
            userService.setPaymentStatus(
                id = userId,
                isPaid = isPaid,
                stripeCustomerId = session.customer
            )
            paymentStatus(userId)
        } catch (e: Exception) {
            AppResult.Failure(HttpStatusCode.InternalServerError, e.message ?: "Stripe error")
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
                            "amount": {
                                "currency_code": "${paypalConfig.currency}",
                                "value": "${paypalConfig.amount}"
                            },
                            "reference_id": "$userId"
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

            val response = paypalHttpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val json = paypalJson.parseToJsonElement(response.body()).jsonObject
            val orderId = json["id"]?.jsonPrimitive?.content ?: ""
            
            AppResult.Success(PayPalOrderResponseDto(orderId))
        } catch (e: Exception) {
            AppResult.Failure(HttpStatusCode.InternalServerError, e.message ?: "PayPal error")
        }
    }

    override suspend fun capturePayPalOrder(userId: UUID, dto: CapturePayPalOrderDto): AppResult<PaymentStatusDto> {
        return try {
            val accessToken = getPayPalAccessToken()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("${paypalConfig.baseUrl}/v2/checkout/orders/${dto.orderId}/capture"))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build()

            val response = paypalHttpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val json = paypalJson.parseToJsonElement(response.body()).jsonObject
            val status = json["status"]?.jsonPrimitive?.content
            val successful = status == "COMPLETED"

            // 1. Create Payment Record
            paymentRepository.create(
                Payment.createNew(
                    userId = userId,
                    paymentType = PaymentType.PAYPAL,
                    successful = successful
                )
            )

            // 2. Update User Status
            userService.setPaymentStatus(
                id = userId,
                isPaid = successful
            )
            
            paymentStatus(userId)
        } catch (e: Exception) {
            AppResult.Failure(HttpStatusCode.InternalServerError, e.message ?: "PayPal error")
        }
    }

    override suspend fun getAll(): AppResult<List<Payment>> {
        return AppResult.Success(paymentRepository.findAll())
    }

    override suspend fun getById(id: UUID): AppResult<Payment> {
        val payment = paymentRepository.findById(id)
        return if (payment != null) {
            AppResult.Success(payment)
        } else {
            AppResult.Failure(HttpStatusCode.NotFound, "Payment not found")
        }
    }

    override suspend fun getByUserId(userId: UUID): AppResult<List<Payment>> {
        return AppResult.Success(paymentRepository.findByUserId(userId))
    }
}
