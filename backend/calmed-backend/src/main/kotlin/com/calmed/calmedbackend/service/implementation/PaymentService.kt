package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.auth.google.GooglePlayRsaVerifier
import com.calmed.calmedbackend.config.GooglePlayConfig
import com.calmed.calmedbackend.config.PayPalConfig
import com.calmed.calmedbackend.config.StripeConfig
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.*
import com.calmed.calmedbackend.model.dto.response.*
import com.calmed.calmedbackend.model.raw.payment.Payment
import com.calmed.calmedbackend.model.raw.payment.PaymentProvider
import com.calmed.calmedbackend.model.raw.payment.PaymentStatus
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlement
import com.calmed.calmedbackend.model.raw.payment.StoreEntitlementProvider
import com.calmed.calmedbackend.repository.specification.IPaymentRepository
import com.calmed.calmedbackend.repository.specification.IStoreEntitlementRepository
import com.calmed.calmedbackend.service.specification.IPaymentService
import com.calmed.calmedbackend.service.specification.IUserService
import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.Base64
import java.util.UUID

class PaymentService(
    private val userService: IUserService,
    private val paymentRepository: IPaymentRepository,
    private val storeEntitlementRepository: IStoreEntitlementRepository,
    private val stripeConfig: StripeConfig,
    private val paypalConfig: PayPalConfig,
    private val googlePlayConfig: GooglePlayConfig
) : IPaymentService {

    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    init {
        Stripe.apiKey = stripeConfig.secretKey
    }

    private val paypalHttpClient = HttpClient.newHttpClient()

    private val paypalJson = Json {
        ignoreUnknownKeys = true
    }

    private suspend fun userHasActivePayment(userId: UUID): Boolean {
        val storeAccess = storeEntitlementRepository.findByUserId(userId).isNotEmpty()
        if (storeAccess) return true

        val payments = paymentRepository.findByUserId(userId)
        return payments.any { it.status == PaymentStatus.SUCCESSFUL && it.refundedAt == null }
    }

    private suspend fun latestActivePaymentProvider(userId: UUID): PaymentProvider? {
        val entitlements = storeEntitlementRepository.findByUserId(userId)
        if (entitlements.isNotEmpty()) {
            return entitlements.first().store.let {
                when (it) {
                    StoreEntitlementProvider.GOOGLE -> PaymentProvider.GOOGLE
                    StoreEntitlementProvider.APPLE -> PaymentProvider.APPLE
                }
            }
        }

        return paymentRepository
            .findByUserId(userId)
            .filter { it.status == PaymentStatus.SUCCESSFUL && it.refundedAt == null }
            .maxByOrNull { it.createdAt }
            ?.provider
    }

    private suspend fun latestPaymentStatus(userId: UUID): PaymentStatus? {
        val entitlements = storeEntitlementRepository.findByUserId(userId)
        if (entitlements.isNotEmpty()) return PaymentStatus.SUCCESSFUL

        return paymentRepository
            .findByUserId(userId)
            .maxByOrNull { it.createdAt }
            ?.status
    }

    private suspend fun grantOrRestoreEntitlement(
        store: StoreEntitlementProvider,
        storeTransactionId: String,
        userId: UUID
    ): EntitlementGrantResult {
        val existing = storeEntitlementRepository.findByStoreTransactionId(store, storeTransactionId)
        return when {
            existing == null -> {
                storeEntitlementRepository.create(
                    StoreEntitlement.createNew(
                        store = store,
                        storeTransactionId = storeTransactionId,
                        userId = userId
                    )
                )
                EntitlementGrantResult.GRANTED
            }
            existing.userId == null -> {
                storeEntitlementRepository.update(
                    existing.copy(userId = userId, updatedAt = Instant.now())
                )
                EntitlementGrantResult.RESTORED
            }
            existing.userId == userId -> EntitlementGrantResult.GRANTED
            else -> EntitlementGrantResult.CONFLICT
        }
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
        return try {
            val userRes = userService.getById(userId)
            when (userRes) {
                is AppResult.Success -> {
                    AppResult.Success(
                        PaymentStatusDto(
                            hasAccess = userHasActivePayment(userId),
                            status = latestPaymentStatus(userId),
                            provider = latestActivePaymentProvider(userId),
                            amount = stripeConfig.amount,
                            currency = stripeConfig.currency
                        )
                    )
                }
                is AppResult.Failure -> AppResult.Failure(userRes.httpStatusCode, userRes.message)
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve payment status for user $userId: ${e.message}", e)
            AppResult.Failure(HttpStatusCode.InternalServerError, "Error retrieving payment status: ${e.message}")
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
            logger.error("Failed to create Stripe checkout session for user $userId: ${e.message}", e)
            AppResult.Failure(HttpStatusCode.InternalServerError, e.message ?: "Stripe error")
        }
    }

    override suspend fun handleStripeWebhook(payload: String, sigHeader: String): AppResult<Unit> {
        return try {
            val event = Webhook.constructEvent(payload, sigHeader, stripeConfig.webhookSecret)
            val session = event.dataObjectDeserializer.getObject().get() as? Session ?: return AppResult.Success(Unit)
            val userIdStr = session.metadata["userId"]

            when (event.type) {
                "checkout.session.completed" -> {
                    if (userIdStr != null) {
                        val userId = UUID.fromString(userIdStr)
                        val isPaid = session.paymentStatus == "paid" || session.status == "complete"
                        val checkoutId = session.id

                        val existing = checkoutId?.let { paymentRepository.findByStripeCheckoutSessionId(it) }
                        if (existing != null) {
                            paymentRepository.update(
                                existing.copy(
                                    status = if (isPaid) PaymentStatus.SUCCESSFUL else PaymentStatus.PENDING,
                                    stripePaymentIntentId = existing.stripePaymentIntentId ?: session.paymentIntent,
                                    updatedAt = Instant.now()
                                )
                            )
                        } else {
                            paymentRepository.create(
                                Payment.createNew(
                                    userId = userId,
                                    provider = PaymentProvider.STRIPE,
                                    stripeCheckoutSessionId = checkoutId,
                                    stripePaymentIntentId = session.paymentIntent,
                                    status = if (isPaid) PaymentStatus.SUCCESSFUL else PaymentStatus.PENDING
                                )
                            )
                        }
                    }
                }
                "checkout.session.expired" -> {
                    if (userIdStr != null) {
                        session.id?.let { checkoutId ->
                            paymentRepository.findByStripeCheckoutSessionId(checkoutId)?.let { existing ->
                                paymentRepository.update(
                                    existing.copy(
                                        status = PaymentStatus.PENDING,
                                        updatedAt = Instant.now()
                                    )
                                )
                            }
                        }
                    }
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            logger.error("Stripe webhook processing failed: ${e.message}", e)
            AppResult.Failure(HttpStatusCode.BadRequest, "Webhook error: ${e.message}")
        }
    }

    override suspend fun skipPayment(userId: UUID): AppResult<PaymentStatusDto> {
        return try {
            paymentRepository.create(
                Payment.createNew(
                    userId = userId,
                    provider = PaymentProvider.SKIP,
                    status = PaymentStatus.SUCCESSFUL
                )
            )
            paymentStatus(userId)
        } catch (e: Exception) {
            logger.error("Failed to process skip payment for user $userId: ${e.message}", e)
            try {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.SKIP,
                        status = PaymentStatus.PENDING
                    )
                )
            } catch (ignored: Exception) {}
            AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to process skip payment: ${e.message}")
        }
    }

    override suspend fun verifyApplePurchase(userId: UUID, dto: VerifyAppleReceiptDto): AppResult<PaymentStatusDto> {
        return try {
            if (dto.transactionId.isBlank()) {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.APPLE,
                        status = PaymentStatus.PENDING
                    )
                )
                return AppResult.Failure(HttpStatusCode.BadRequest, "Transaction ID cannot be blank")
            }


            val entitlementTransactionId = dto.transactionId

            val grant = grantOrRestoreEntitlement(
                store = StoreEntitlementProvider.APPLE,
                storeTransactionId = entitlementTransactionId,
                userId = userId
            )

            if (grant == EntitlementGrantResult.CONFLICT) {
                logger.warn("Replay attack detected: Apple transaction $entitlementTransactionId already claimed by another account")
                return AppResult.Failure(
                    HttpStatusCode.Conflict,
                    "This Apple transaction has already been redeemed by another account"
                )
            }

            val existingLedger = paymentRepository.findByAppleTransactionId(dto.transactionId)
            if (existingLedger == null) {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.APPLE,
                        appleTransactionId = dto.transactionId,
                        appleOriginalTransactionId = dto.transactionId,
                        status = PaymentStatus.SUCCESSFUL
                    )
                )
            }

            paymentStatus(userId)
        } catch (e: Exception) {
            logger.error("Failed to verify Apple purchase for user $userId: ${e.message}", e)
            try {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.APPLE,
                        appleTransactionId = dto.transactionId.takeIf { it.isNotBlank() },
                        status = PaymentStatus.PENDING
                    )
                )
            } catch (ignored: Exception) {}
            AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to verify Apple purchase: ${e.message}")
        }
    }

    override suspend fun verifyGooglePurchase(userId: UUID, dto: VerifyGoogleReceiptDto): AppResult<PaymentStatusDto> {
        return try {
            var extractedOrderId = dto.orderId
            var extractedProductId = dto.productId
            var extractedToken = dto.purchaseToken

            if (dto.purchaseData.isNotBlank()) {
                val purchaseJson = try {
                    Json.parseToJsonElement(dto.purchaseData).jsonObject
                } catch (e: Exception) {
                    logger.warn("Invalid purchaseData JSON received for user $userId: ${dto.purchaseData}")
                    paymentRepository.create(
                        Payment.createNew(
                            userId = userId,
                            provider = PaymentProvider.GOOGLE,
                            status = PaymentStatus.PENDING
                        )
                    )
                    return AppResult.Failure(HttpStatusCode.BadRequest, "Invalid Google purchase data JSON format")
                }

                val packageName = purchaseJson["packageName"]?.jsonPrimitive?.content
                if (googlePlayConfig.packageName.isNotBlank() && packageName != null && packageName != googlePlayConfig.packageName) {
                    logger.warn("Package name mismatch in purchaseData: expected '${googlePlayConfig.packageName}', got '$packageName'")
                    paymentRepository.create(
                        Payment.createNew(
                            userId = userId,
                            provider = PaymentProvider.GOOGLE,
                            status = PaymentStatus.PENDING
                        )
                    )
                    return AppResult.Failure(HttpStatusCode.BadRequest, "Package name mismatch in purchase data")
                }

                val purchaseState = purchaseJson["purchaseState"]?.jsonPrimitive?.content?.toIntOrNull()
                if (purchaseState != null && purchaseState != 0) {
                    logger.warn("Google purchase is not in PURCHASED state (state=$purchaseState) for user $userId")
                    val orderIdFromData = purchaseJson["orderId"]?.jsonPrimitive?.content ?: extractedOrderId
                    val tokenFromData = purchaseJson["purchaseToken"]?.jsonPrimitive?.content ?: extractedToken
                    paymentRepository.create(
                        Payment.createNew(
                            userId = userId,
                            provider = PaymentProvider.GOOGLE,
                            googleOrderId = orderIdFromData.takeIf { it.isNotBlank() },
                            googlePurchaseToken = tokenFromData.takeIf { it.isNotBlank() },
                            status = PaymentStatus.PENDING
                        )
                    )
                    return AppResult.Failure(
                        HttpStatusCode.PaymentRequired,
                        "Google purchase is not in completed/purchased state (state: $purchaseState)"
                    )
                }

                if (extractedOrderId.isBlank()) {
                    extractedOrderId = purchaseJson["orderId"]?.jsonPrimitive?.content ?: ""
                }
                if (extractedProductId.isBlank()) {
                    extractedProductId = purchaseJson["productId"]?.jsonPrimitive?.content ?: ""
                }
                if (extractedToken.isBlank()) {
                    extractedToken = purchaseJson["purchaseToken"]?.jsonPrimitive?.content ?: ""
                }
            }

            if (extractedOrderId.isBlank() && extractedToken.isBlank() && dto.purchaseData.isBlank()) {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.GOOGLE,
                        status = PaymentStatus.PENDING
                    )
                )
                return AppResult.Failure(HttpStatusCode.BadRequest, "Google purchase token or order ID is required")
            }

            val finalOrderId = extractedOrderId.ifBlank { extractedToken }
            val finalToken = extractedToken.ifBlank { extractedOrderId }

            val publicKey = googlePlayConfig.publicKey.trim()
            if (publicKey.isNotBlank() && publicKey != "placeholder") {
                if (dto.purchaseData.isBlank() || dto.signature.isBlank()) {
                    logger.warn("RSA verification required but purchaseData or signature missing for user $userId")
                    paymentRepository.create(
                        Payment.createNew(
                            userId = userId,
                            provider = PaymentProvider.GOOGLE,
                            googleOrderId = finalOrderId.takeIf { it.isNotBlank() },
                            googlePurchaseToken = finalToken.takeIf { it.isNotBlank() },
                            status = PaymentStatus.PENDING
                        )
                    )
                    return AppResult.Failure(
                        HttpStatusCode.Unauthorized,
                        "Google Play purchase data and cryptographic signature are required for verification"
                    )
                }

                val isSignatureValid = GooglePlayRsaVerifier.verify(
                    encodedPublicKey = publicKey,
                    signedData = dto.purchaseData,
                    signature = dto.signature
                )

                if (!isSignatureValid) {
                    logger.error("Invalid Google Play RSA signature for user $userId (orderId: $finalOrderId)")
                    paymentRepository.create(
                        Payment.createNew(
                            userId = userId,
                            provider = PaymentProvider.GOOGLE,
                            googleOrderId = finalOrderId.takeIf { it.isNotBlank() },
                            googlePurchaseToken = finalToken.takeIf { it.isNotBlank() },
                            status = PaymentStatus.PENDING
                        )
                    )
                    return AppResult.Failure(HttpStatusCode.Unauthorized, "Invalid Google Play purchase signature")
                }
                logger.info("Successfully verified Google Play RSA signature for order $finalOrderId")
            } else {
                logger.info("Google Play public key not configured or set to placeholder; skipping RSA signature check for user $userId")
            }

            val entitlementTransactionId = finalToken.ifBlank { finalOrderId }

            val grant = grantOrRestoreEntitlement(
                store = StoreEntitlementProvider.GOOGLE,
                storeTransactionId = entitlementTransactionId,
                userId = userId
            )

            if (grant == EntitlementGrantResult.CONFLICT) {
                logger.warn(
                    "Replay attack detected: Google purchase $entitlementTransactionId already claimed by another account, attempted by $userId"
                )
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.GOOGLE,
                        status = PaymentStatus.PENDING
                    )
                )
                return AppResult.Failure(
                    HttpStatusCode.Conflict,
                    "This Google Play order has already been redeemed by another account"
                )
            }

            val existingLedger = finalOrderId.takeIf { it.isNotBlank() }
                ?.let { paymentRepository.findByGoogleOrderId(it) }
            if (existingLedger == null) {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.GOOGLE,
                        googleOrderId = finalOrderId,
                        googlePurchaseToken = finalToken,
                        status = PaymentStatus.SUCCESSFUL
                    )
                )
            }

            paymentStatus(userId)
        } catch (e: Exception) {
            logger.error("Exception during Google purchase processing for user $userId: ${e.message}", e)
            val orderId = dto.orderId.ifBlank { dto.purchaseToken }
            try {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.GOOGLE,
                        googleOrderId = orderId.takeIf { it.isNotBlank() },
                        googlePurchaseToken = dto.purchaseToken.takeIf { it.isNotBlank() },
                        status = PaymentStatus.PENDING
                    )
                )
            } catch (ignored: Exception) {}
            AppResult.Failure(HttpStatusCode.InternalServerError, "Error processing Google purchase: ${e.message}")
        }
    }

    override suspend fun verifyStripeSession(userId: UUID, sessionId: String): AppResult<PaymentStatusDto> {
        return try {
            val session = Session.retrieve(sessionId)
            val isPaid = session.paymentStatus == "paid" || session.status == "complete"

            val existing = paymentRepository.findByStripeCheckoutSessionId(sessionId)
            if (existing != null) {
                paymentRepository.update(
                    existing.copy(
                        status = if (isPaid) PaymentStatus.SUCCESSFUL else PaymentStatus.PENDING,
                        stripePaymentIntentId = existing.stripePaymentIntentId ?: session.paymentIntent,
                        updatedAt = Instant.now()
                    )
                )
            } else {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.STRIPE,
                        stripeCheckoutSessionId = sessionId,
                        stripePaymentIntentId = session.paymentIntent,
                        status = if (isPaid) PaymentStatus.SUCCESSFUL else PaymentStatus.PENDING
                    )
                )
            }

            if (isPaid) {
                paymentStatus(userId)
            } else {
                AppResult.Failure(
                    HttpStatusCode.PaymentRequired,
                    "Stripe session payment status is not paid (status: ${session.paymentStatus})"
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to verify Stripe session $sessionId for user $userId: ${e.message}", e)
            try {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.STRIPE,
                        stripeCheckoutSessionId = sessionId,
                        status = PaymentStatus.PENDING
                    )
                )
            } catch (ignored: Exception) {}
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

            val response = withContext(Dispatchers.IO) {
	            paypalHttpClient.send(request, HttpResponse.BodyHandlers.ofString())
            }
            val json = paypalJson.parseToJsonElement(response.body()).jsonObject
            val orderId = json["id"]?.jsonPrimitive?.content ?: ""

            AppResult.Success(PayPalOrderResponseDto(orderId))
        } catch (e: Exception) {
            logger.error("Failed to create PayPal order for user $userId: ${e.message}", e)
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

            val response = withContext(Dispatchers.IO) {
	            paypalHttpClient.send(request, HttpResponse.BodyHandlers.ofString())
            }
            val json = paypalJson.parseToJsonElement(response.body()).jsonObject
            val status = json["status"]?.jsonPrimitive?.content
            val successful = status == "COMPLETED"

            val captureId = json["purchase_units"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("payments")
                ?.jsonObject
                ?.get("captures")
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("id")
                ?.jsonPrimitive
                ?.content

            val existing = paymentRepository.findByPayPalOrderId(dto.orderId)
            if (existing != null) {
                paymentRepository.update(
                    existing.copy(
                        status = if (successful) PaymentStatus.SUCCESSFUL else PaymentStatus.PENDING,
                        paypalCaptureId = captureId ?: existing.paypalCaptureId,
                        updatedAt = Instant.now()
                    )
                )
            } else {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.PAYPAL,
                        paypalOrderId = dto.orderId,
                        paypalCaptureId = captureId,
                        status = if (successful) PaymentStatus.SUCCESSFUL else PaymentStatus.PENDING
                    )
                )
            }

            if (successful) {
                paymentStatus(userId)
            } else {
                AppResult.Failure(
                    HttpStatusCode.PaymentRequired,
                    "PayPal order capture not completed (status: ${status ?: "UNKNOWN"})"
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to capture PayPal order ${dto.orderId} for user $userId: ${e.message}", e)
            try {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.PAYPAL,
                        paypalOrderId = dto.orderId,
                        status = PaymentStatus.PENDING
                    )
                )
            } catch (ignored: Exception) {}
            AppResult.Failure(HttpStatusCode.InternalServerError, e.message ?: "PayPal error")
        }
    }

    override suspend fun getAll(): AppResult<List<Payment>> {
        return try {
            AppResult.Success(paymentRepository.findAll())
        } catch (e: Exception) {
            logger.error("Failed to fetch all payments: ${e.message}", e)
            AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to retrieve payments: ${e.message}")
        }
    }

    override suspend fun getById(id: UUID): AppResult<Payment> {
        return try {
            val payment = paymentRepository.findById(id)
            if (payment != null) {
                AppResult.Success(payment)
            } else {
                AppResult.Failure(HttpStatusCode.NotFound, "Payment not found")
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch payment $id: ${e.message}", e)
            AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to retrieve payment: ${e.message}")
        }
    }

    override suspend fun getByUserId(userId: UUID): AppResult<List<Payment>> {
        return try {
            AppResult.Success(paymentRepository.findByUserId(userId))
        } catch (e: Exception) {
            logger.error("Failed to fetch payments for user $userId: ${e.message}", e)
            AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to retrieve payments for user: ${e.message}")
        }
    }
}
