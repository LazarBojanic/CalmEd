package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmed.calmedtics.Res
import com.calmed.calmedtics.amount_label
import com.calmed.calmedtics.getPlatform
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedtics.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedtics.model.raw.PaymentType
import com.calmed.calmedtics.billing.BillingProducts
import com.calmed.calmedtics.billing.BillingService
import com.calmed.calmedtics.billing.PurchaseResult
import com.calmed.calmedtics.billing.provideBillingService
import com.calmed.calmedtics.error_init_payment
import com.calmed.calmedtics.error_payment_not_confirmed
import com.calmed.calmedtics.error_payment_verification
import com.calmed.calmedtics.error_skip_payment
import com.calmed.calmedtics.opening_payment
import com.calmed.calmedtics.pay_button
import com.calmed.calmedtics.payment_description
import com.calmed.calmedtics.payment_heading
import com.calmed.calmedtics.payment_title
import com.calmed.calmedtics.premium_access
import com.calmed.calmedtics.processing
import com.calmed.calmedtics.secure_payment
import com.calmed.calmedtics.skip_payment
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun PaymentScreen(
    onPaid: () -> Unit,
    sessionViewModel: SessionViewModel = koinInject(),
    api: IAppApi = koinInject(),
    billingService: BillingService = remember { provideBillingService() }
) {
    val scope = rememberCoroutineScope()

    val platformName = remember { getPlatform().name }
    val isAndroid = remember { platformName.startsWith("Android", ignoreCase = true) }
    val storeName = if (isAndroid) "Google Play" else "App Store"

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var priceLabel by remember { mutableStateOf("$10.00 (USD)") }
    val errorInitPayment = stringResource(Res.string.error_init_payment)
    val errorSkipPayment = stringResource(Res.string.error_skip_payment)
    val errorPaymentNotConfirmed = stringResource(Res.string.error_payment_not_confirmed)
    val errorpayementVerification = stringResource(Res.string.error_payment_verification)
    fun startNativePayment() {

        scope.launch {
            loading = true
            error = null
            try {
                billingService.purchase(BillingProducts.PREMIUM_ONE_TIME)
            } catch (t: Throwable) {
                error = t.message ?: errorInitPayment
                loading = false
            }
        }
    }

    fun skipPayment() {
        scope.launch {
            loading = true
            error = null
            try {
                api.skipPayment()
                val user = sessionViewModel.loadSession()
                if (user?.isPaid == true) onPaid() else error = errorSkipPayment
            } catch (t: Throwable) {
                error = t.message ?: errorSkipPayment
            } finally {
                loading = false
            }
        }
    }

    DisposableEffect(Unit) {
        val job = scope.launch {
            billingService.purchaseResults.collectLatest { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        loading = true
                        error = null
                        try {
                            when (result.paymentType) {
                                PaymentType.APPLE -> {
                                    api.verifyApplePurchase(
                                        VerifyAppleReceiptDto(
                                            transactionId = result.appleTransactionId ?: "",
                                            productId = result.productId
                                        )
                                    )
                                }
                                PaymentType.GOOGLE -> {
                                    api.verifyGooglePurchase(
                                        VerifyGoogleReceiptDto(
                                            orderId = result.googleOrderId ?: "",
                                            productId = result.productId,
                                            purchaseToken = result.googlePurchaseToken ?: ""
                                        )
                                    )
                                }
                                else -> {}
                            }
                            val user = sessionViewModel.loadSession()
                            if (user?.isPaid == true) onPaid() else  error = errorPaymentNotConfirmed
                        } catch (t: Throwable) {
                            error = t.message ?: errorpayementVerification
                        } finally {
                            loading = false
                        }
                    }
                    is PurchaseResult.Failure -> {
                        error = result.message
                        loading = false
                    }
                }
            }
        }
        onDispose { job.cancel() }
    }

    ScreenScaffold(title = stringResource(Res.string.payment_title)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.payment_heading),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(Res.string.payment_description, storeName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ReceiptLong, contentDescription = null)
                    Text(
                        stringResource(Res.string.premium_access),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    stringResource(Res.string.amount_label, priceLabel),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, contentDescription = null)
                    Text(
                        text = "  " + stringResource(Res.string.secure_payment, storeName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { startNativePayment() },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (loading) stringResource(Res.string.opening_payment) else  stringResource(Res.string.pay_button, "$10.00"))
            }

            Button(
                onClick = { skipPayment() },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(if (loading) stringResource(Res.string.processing) else stringResource(Res.string.skip_payment))
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
