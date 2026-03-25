package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.config.StripeConfig
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.response.PaymentSheetParamsDto
import com.calmed.calmedbackend.model.dto.response.PaymentStatusDto
import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.service.specification.IPaymentService
import com.calmed.calmedbackend.service.specification.IUserService
import com.stripe.Stripe
import com.stripe.model.Customer
import com.stripe.model.EphemeralKey
import com.stripe.model.PaymentIntent
import com.stripe.net.RequestOptions
import com.stripe.param.CustomerCreateParams
import com.stripe.param.EphemeralKeyCreateParams
import com.stripe.param.PaymentIntentCreateParams
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

    override suspend fun createPaymentSheetParams(
        userId: UUID,
        paymentType: PaymentType
    ): AppResult<PaymentSheetParamsDto> {
        if (stripeConfig.secretKey.isBlank() || stripeConfig.secretKey == "placeholder") {
            return AppResult.Failure(HttpStatusCode.InternalServerError, "Stripe is not configured.")
        }
        if (stripeConfig.publishableKey.isBlank() || stripeConfig.publishableKey == "placeholder") {
            return AppResult.Failure(HttpStatusCode.InternalServerError, "Stripe publishable key is not configured.")
        }

        val userResult = userService.getById(userId)
        val user = when (userResult) {
            is AppResult.Success -> userResult.data
            is AppResult.Failure -> return AppResult.Failure(userResult.httpStatusCode, userResult.message)
        }

        if (user.isPaid) {
            return AppResult.Failure(HttpStatusCode.Conflict, "User is already paid.")
        }

        return try {
            val stripeCustomerId = if (!user.stripeCustomerId.isNullOrBlank()) {
                user.stripeCustomerId
            } else {
                Customer.create(
                    CustomerCreateParams.builder()
                        .setEmail(user.email)
                        .putMetadata("user_id", user.id.toString())
                        .build()
                ).id
            }

            if (stripeCustomerId.isNullOrBlank()) {
                return AppResult.Failure(HttpStatusCode.BadGateway, "Failed to create Stripe customer.")
            }

            val userUpdateResult = userService.setPaymentStatus(
                id = userId,
                isPaid = false,
                paymentType = paymentType,
                stripeCustomerId = stripeCustomerId
            )
            if (userUpdateResult is AppResult.Failure) {
                return AppResult.Failure(userUpdateResult.httpStatusCode, userUpdateResult.message)
            }

            val ephemeralKey = EphemeralKey.create(
                EphemeralKeyCreateParams.builder()
                    .setCustomer(stripeCustomerId)
                    .setStripeVersion(stripeConfig.apiVersion)
                    .build(),
                RequestOptions.RequestOptionsBuilder.unsafeSetStripeVersionOverride(
                    RequestOptions.builder(),
                    stripeConfig.apiVersion
                ).build()
            )
            val ephemeralKeySecret = ephemeralKey.secret
                ?: return AppResult.Failure(HttpStatusCode.BadGateway, "Failed to create ephemeral key.")

            val paymentIntent = PaymentIntent.create(
                PaymentIntentCreateParams.builder()
                    .setAmount(stripeConfig.amountCents)
                    .setCurrency(stripeConfig.currency)
                    .setCustomer(stripeCustomerId)
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .putMetadata("user_id", user.id.toString())
                    .putMetadata("payment_type_requested", paymentType.name)
                    .build()
            )
            val clientSecret = paymentIntent.clientSecret
                ?: return AppResult.Failure(HttpStatusCode.BadGateway, "Payment intent missing client secret.")
            val paymentIntentId = paymentIntent.id
                ?: return AppResult.Failure(HttpStatusCode.BadGateway, "Payment intent id missing.")

            AppResult.Success(
                PaymentSheetParamsDto(
                    paymentIntentId = paymentIntentId,
                    paymentIntentClientSecret = clientSecret,
                    customerId = stripeCustomerId,
                    customerEphemeralKeySecret = ephemeralKeySecret,
                    publishableKey = stripeConfig.publishableKey,
                    merchantDisplayName = stripeConfig.merchantDisplayName,
                    merchantCountryCode = stripeConfig.merchantCountryCode,
                    appleMerchantId = stripeConfig.appleMerchantId,
                    paymentType = paymentType,
                    amountCents = stripeConfig.amountCents,
                    currency = stripeConfig.currency
                )
            )
        } catch (t: Throwable) {
            AppResult.Failure(HttpStatusCode.BadGateway, "Stripe payment setup failed: ${t.message}")
        }
    }

    override suspend fun confirmPaymentIntent(userId: UUID, paymentIntentId: String): AppResult<PaymentStatusDto> {
        if (paymentIntentId.isBlank()) {
            return AppResult.Failure(HttpStatusCode.BadRequest, "Payment intent id is required.")
        }
        val userResult = userService.getById(userId)
        val user = when (userResult) {
            is AppResult.Success -> userResult.data
            is AppResult.Failure -> return AppResult.Failure(userResult.httpStatusCode, userResult.message)
        }

        return try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            val userIdFromStripe = paymentIntent.metadata["user_id"]
            if (userIdFromStripe != userId.toString()) {
                return AppResult.Failure(HttpStatusCode.Forbidden, "Payment intent does not belong to this user.")
            }

            val succeeded = paymentIntent.status.equals("succeeded", ignoreCase = true)
            if (succeeded) {
                val actualType = resolvePaymentType(paymentIntent, fallback = user.paymentType)
                val updateResult = userService.setPaymentStatus(
                    id = userId,
                    isPaid = true,
                    paymentType = actualType,
                    stripeCustomerId = paymentIntent.customer ?: user.stripeCustomerId
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
                AppResult.Success(
                    PaymentStatusDto(
                        isPaid = user.isPaid,
                        paymentType = user.paymentType,
                        amountCents = stripeConfig.amountCents,
                        currency = stripeConfig.currency
                    )
                )
            }
        } catch (t: Throwable) {
            AppResult.Failure(HttpStatusCode.BadGateway, "Stripe payment confirmation failed: ${t.message}")
        }
    }

    private fun resolvePaymentType(paymentIntent: PaymentIntent, fallback: PaymentType?): PaymentType {
        val requested = paymentIntent.metadata["payment_type_requested"]
        return requested?.let { runCatching { PaymentType.valueOf(it) }.getOrNull() }
            ?: fallback
            ?: PaymentType.CARD
    }

    override suspend fun skipPayment(userId: UUID): AppResult<PaymentStatusDto> {
        return when (val res = userService.setPaymentStatus(userId, true, null, null)) {
            is AppResult.Success -> AppResult.Success(PaymentStatusDto(isPaid = true, paymentType = null, currency = stripeConfig.currency, amountCents = stripeConfig.amountCents))
            is AppResult.Failure -> AppResult.Failure(res.httpStatusCode, res.message)
        }
    }
}
