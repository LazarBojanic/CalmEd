package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.config.AppleConfig
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
import com.calmed.calmedbackend.payment.apple.AppStoreServerApi
import com.calmed.calmedbackend.payment.google.GooglePlayDeveloperApi
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
    private val googlePlayConfig: GooglePlayConfig,
    private val appleConfig: AppleConfig
) : IPaymentService {

    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    init {
        Stripe.apiKey = stripeConfig.secretKey
    }

    private val paypalHttpClient = HttpClient.newHttpClient()

    private val paypalJson = Json {
        ignoreUnknownKeys = true
    }

    private val googlePlayDeveloperApi = GooglePlayDeveloperApi(googlePlayConfig.serviceAccountJson)

    private val appStoreServerApi = AppStoreServerApi(
        issuerId = appleConfig.iapIssuerId,
        keyId = appleConfig.iapKeyId,
        privateKeyPem = appleConfig.iapPrivateKeyPem,
        bundleId = appleConfig.iosBundleId
    )

    private suspend fun userHasActivePayment(userId: UUID): Boolean {
        val storeAccess = storeEntitlementRepository.findByUserId(userId).any { it.revokedAt == null }
        if (storeAccess) return true

        val payments = paymentRepository.findByUserId(userId)
        return payments.any { it.status == PaymentStatus.SUCCESSFUL && it.refundedAt == null }
    }

    private suspend fun latestActivePaymentProvider(userId: UUID): PaymentProvider? {
        val entitlements = storeEntitlementRepository.findByUserId(userId).filter { it.revokedAt == null }
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
        val entitlements = storeEntitlementRepository.findByUserId(userId).filter { it.revokedAt == null }
        if (entitlements.isNotEmpty()) return PaymentStatus.SUCCESSFUL

        return paymentRepository
            .findByUserId(userId)
            .maxByOrNull { it.createdAt }
            ?.status
    }

    private suspend fun grantOrRestoreEntitlement(
        store: StoreEntitlementProvider,
        storeTransactionId: String,
        userId: UUID,
        productId: String? = null,
        obfuscatedAccountId: String? = null,
        environment: String? = null
    ): EntitlementGrantResult {
        val existing = storeEntitlementRepository.findByStoreTransactionId(store, storeTransactionId)
        return when {
            existing == null -> {
                storeEntitlementRepository.create(
                    StoreEntitlement.createNew(
                        store = store,
                        storeTransactionId = storeTransactionId,
                        userId = userId,
                        productId = productId,
                        obfuscatedAccountId = obfuscatedAccountId,
                        environment = environment
                    )
                )
                EntitlementGrantResult.GRANTED
            }
            existing.userId == null -> {
                storeEntitlementRepository.update(
                    existing.copy(
                        userId = userId,
                        productId = productId ?: existing.productId,
                        obfuscatedAccountId = obfuscatedAccountId ?: existing.obfuscatedAccountId,
                        environment = environment ?: existing.environment,
                        revokedAt = null,
                        updatedAt = Instant.now()
                    )
                )
                EntitlementGrantResult.RESTORED
            }
            existing.userId == userId -> {
                storeEntitlementRepository.update(
                    existing.copy(
                        productId = productId ?: existing.productId,
                        obfuscatedAccountId = obfuscatedAccountId ?: existing.obfuscatedAccountId,
                        environment = environment ?: existing.environment,
                        revokedAt = null,
                        updatedAt = Instant.now()
                    )
                )
                EntitlementGrantResult.GRANTED
            }
            else -> EntitlementGrantResult.CONFLICT
        }
    }

    private fun isProductAllowed(productId: String): Boolean {
        val expected = googlePlayConfig.productId.ifBlank { appleConfig.productId }
        return expected.isBlank() || productId == expected
    }

    private suspend fun userEmail(userId: UUID): String? {
        return when (val res = userService.getById(userId)) {
            is AppResult.Success -> res.data.email
            is AppResult.Failure -> null
        }
    }

    private fun obfuscateAccountId(identifier: String): String {
        var hash = 0xcbf29ce484222325UL
        for (byte in identifier.trim().lowercase().toByteArray(Charsets.UTF_8)) {
            hash = hash xor (byte.toInt() and 0xFF).toULong()
            hash = hash * 0x100000001b3UL
        }
        return hash.toString(16).padStart(16, '0')
    }

    private fun looksLikeValidGooglePurchase(purchaseToken: String): Boolean {
        if (purchaseToken.isBlank()) return false
        val staticIds = listOf(
            "android.test.purchased",
            "android.test.canceled",
            "android.test.refunded",
            "android.test.item_unavailable"
        )
        return staticIds.none { purchaseToken.contains(it) }
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

    override suspend fun verifyApplePurchase(userId: UUID, dto: VerifyAppleReceiptDto): AppResult<PaymentStatusDto> {
        return try {
            if (dto.transactionId.isBlank()) {
                return AppResult.Failure(HttpStatusCode.BadRequest, "Transaction ID cannot be blank")
            }
            if (dto.productId.isBlank()) {
                return AppResult.Failure(HttpStatusCode.BadRequest, "Product ID is required")
            }
            if (!isProductAllowed(dto.productId)) {
                logger.warn("Unexpected Apple product '${dto.productId}' for user $userId")
                return AppResult.Failure(HttpStatusCode.BadRequest, "Unknown product")
            }
            if (!appStoreServerApi.isConfigured()) {
                logger.error("Apple App Store Server API is not configured; cannot verify transaction ${dto.transactionId}")
                return AppResult.Failure(HttpStatusCode.ServiceUnavailable, "Apple purchase verification is not configured")
            }

            val verified = appStoreServerApi.getVerifiedTransaction(dto.transactionId)
                ?: return AppResult.Failure(HttpStatusCode.PaymentRequired, "Apple transaction could not be verified")

            if (verified.revocationDate != null) {
                logger.warn("Apple transaction ${verified.transactionId} is revoked; rejecting for user $userId")
                return AppResult.Failure(HttpStatusCode.PaymentRequired, "This purchase has been refunded or revoked")
            }
            if (verified.bundleId.isNotBlank() && appleConfig.iosBundleId.isNotBlank() &&
                verified.bundleId != appleConfig.iosBundleId
            ) {
                logger.warn("Apple transaction bundle mismatch: '${verified.bundleId}' vs '${appleConfig.iosBundleId}'")
                return AppResult.Failure(HttpStatusCode.BadRequest, "Bundle ID mismatch")
            }

            val entitlementTransactionId = verified.originalTransactionId.ifBlank { verified.transactionId }

            val finalProductId = verified.productId.ifBlank { dto.productId }
            if (!isProductAllowed(finalProductId)) {
                logger.warn("Unexpected Apple product '$finalProductId' for user $userId")
                return AppResult.Failure(HttpStatusCode.BadRequest, "Unknown product")
            }

            val grant = grantOrRestoreEntitlement(
                store = StoreEntitlementProvider.APPLE,
                storeTransactionId = entitlementTransactionId,
                userId = userId,
                productId = finalProductId,
                environment = verified.environment
            )
            if (grant == EntitlementGrantResult.CONFLICT) {
                logger.warn("Apple transaction $entitlementTransactionId already claimed by another account")
                return AppResult.Failure(HttpStatusCode.Conflict, "This Apple transaction has already been redeemed by another account")
            }

            val existingLedger = paymentRepository.findByAppleTransactionId(entitlementTransactionId)
            if (existingLedger == null) {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.APPLE,
                        appleTransactionId = verified.transactionId,
                        appleOriginalTransactionId = entitlementTransactionId,
                        status = PaymentStatus.SUCCESSFUL
                    )
                )
            } else if (existingLedger.userId == null || existingLedger.userId != userId) {
                paymentRepository.update(existingLedger.copy(userId = userId, updatedAt = Instant.now()))
            }

            paymentStatus(userId)
        } catch (e: Exception) {
            logger.error("Failed to verify Apple purchase for user $userId: ${e.message}", e)
            AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to verify Apple purchase: ${e.message}")
        }
    }

    override suspend fun verifyGooglePurchase(userId: UUID, dto: VerifyGoogleReceiptDto): AppResult<PaymentStatusDto> {
        return try {
            var productId = dto.productId
            var purchaseToken = dto.purchaseToken

            if (dto.purchaseData.isNotBlank()) {
                val purchaseJson = runCatching { Json.parseToJsonElement(dto.purchaseData).jsonObject }.getOrNull()
                if (purchaseJson != null) {
                    if (productId.isBlank()) productId = purchaseJson["productId"]?.jsonPrimitive?.content ?: ""
                    if (purchaseToken.isBlank()) purchaseToken = purchaseJson["purchaseToken"]?.jsonPrimitive?.content ?: ""
                }
            }

            if (purchaseToken.isBlank()) {
                return AppResult.Failure(HttpStatusCode.BadRequest, "Google purchase token is required")
            }
            if (productId.isBlank()) {
                return AppResult.Failure(HttpStatusCode.BadRequest, "Product ID is required")
            }
            if (!isProductAllowed(productId)) {
                logger.warn("Unexpected Google product '$productId' for user $userId")
                return AppResult.Failure(HttpStatusCode.BadRequest, "Unknown product")
            }
            val apiConfigured = googlePlayDeveloperApi.isConfigured()

            if (!apiConfigured && !googlePlayConfig.devFallbackEnabled) {
                logger.error("Google Play Developer API is not configured; cannot verify purchase for user $userId")
                return AppResult.Failure(HttpStatusCode.ServiceUnavailable, "Google purchase verification is not configured")
            }

            var serverPurchase: GooglePlayDeveloperApi.ProductPurchase? = null
            if (apiConfigured) {
                serverPurchase = try {
                    googlePlayDeveloperApi.validateProductPurchase(
                        packageName = googlePlayConfig.packageName,
                        productId = productId,
                        token = purchaseToken
                    )
                } catch (e: Exception) {
                    logger.warn("Google Play Developer API validation failed for user $userId: ${e.message}")
                    null
                }
            }

            val orderId: String?
            val obfuscatedAccountId: String?
            var environment: String? = null

            when {
                serverPurchase != null && serverPurchase.purchaseState == 0 -> {
                    orderId = serverPurchase.orderId ?: dto.orderId
                    obfuscatedAccountId = serverPurchase.obfuscatedExternalAccountId

                    val obfuscated = serverPurchase.obfuscatedExternalAccountId
                    if (!obfuscated.isNullOrBlank()) {
                        val expected = userEmail(userId)?.let { obfuscateAccountId(it) }
                        if (expected != null && expected != obfuscated) {
                            logger.warn("Google purchase obfuscated account mismatch: expected $expected, got $obfuscated for user $userId")
                        }
                    }
                }
                serverPurchase != null -> {
                    logger.warn("Google purchase state ${serverPurchase.purchaseState} for user $userId (not purchased)")
                    return AppResult.Failure(HttpStatusCode.PaymentRequired, "Google purchase is not in a purchased state")
                }
                googlePlayConfig.devFallbackEnabled && looksLikeValidGooglePurchase(purchaseToken) -> {
                    logger.warn("DEV-ONLY: granting Google entitlement without Play API validation for user $userId (product '$productId')")
                    orderId = dto.orderId
                    obfuscatedAccountId = null
                    environment = "DEV_LOCAL"
                }
                else -> {
                    return AppResult.Failure(HttpStatusCode.PaymentRequired, "Google purchase could not be verified")
                }
            }

            val grant = grantOrRestoreEntitlement(
                store = StoreEntitlementProvider.GOOGLE,
                storeTransactionId = purchaseToken,
                userId = userId,
                productId = productId,
                obfuscatedAccountId = obfuscatedAccountId,
                environment = environment
            )
            if (grant == EntitlementGrantResult.CONFLICT) {
                logger.warn("Google purchase $purchaseToken already claimed by another account, attempted by $userId")
                return AppResult.Failure(HttpStatusCode.Conflict, "This Google Play purchase has already been redeemed by another account")
            }

            val ledgerKey = orderId.takeIf { it.isNotBlank() } ?: purchaseToken
            val existingLedger = paymentRepository.findByGoogleOrderId(ledgerKey)
            if (existingLedger == null) {
                paymentRepository.create(
                    Payment.createNew(
                        userId = userId,
                        provider = PaymentProvider.GOOGLE,
                        googleOrderId = orderId,
                        googlePurchaseToken = purchaseToken,
                        status = PaymentStatus.SUCCESSFUL
                    )
                )
            } else if (existingLedger.userId == null || existingLedger.userId != userId) {
                paymentRepository.update(existingLedger.copy(userId = userId, updatedAt = Instant.now()))
            }

            paymentStatus(userId)
        } catch (e: Exception) {
            logger.error("Exception during Google purchase processing for user $userId: ${e.message}", e)
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
