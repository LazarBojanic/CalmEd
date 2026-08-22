package com.calmed.calmedtics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.amount_label
import com.calmed.calmedtics.getPlatform
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedtics.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedtics.model.raw.PaymentProvider
import com.calmed.calmedtics.billing.BillingProducts
import com.calmed.calmedtics.billing.BillingService
import com.calmed.calmedtics.billing.PurchaseResult
import com.calmed.calmedtics.billing.provideBillingService
import com.calmed.calmedtics.billing.obfuscateAccountId
import calmedtics.shared.generated.resources.error_init_payment
import calmedtics.shared.generated.resources.error_payment_not_confirmed
import calmedtics.shared.generated.resources.error_payment_verification
import calmedtics.shared.generated.resources.logout
import calmedtics.shared.generated.resources.opening_payment
import calmedtics.shared.generated.resources.pay_button
import calmedtics.shared.generated.resources.payment_description
import calmedtics.shared.generated.resources.payment_heading
import calmedtics.shared.generated.resources.premium_access
import calmedtics.shared.generated.resources.processing
import calmedtics.shared.generated.resources.secure_payment
import com.calmed.calmedtics.theme.appBackgroundGradient
import com.calmed.calmedtics.ui.component.PrimaryButton
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun PaymentScreen(
    onPaid: () -> Unit,
    onLogout: () -> Unit = {},
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
    var info by remember { mutableStateOf<String?>(null) }
    var priceLabel by remember { mutableStateOf("$5.00 (EUR)") }
    val errorInitPayment = stringResource(Res.string.error_init_payment)
    val errorPaymentNotConfirmed = stringResource(Res.string.error_payment_not_confirmed)
    val errorPaymentVerification = stringResource(Res.string.error_payment_verification)
    fun startNativePayment() {

        scope.launch {
            loading = true
            error = null
            try {
                val accountId = sessionViewModel.user.value?.email
                    ?.takeIf { it.isNotBlank() }
                    ?.let { obfuscateAccountId(it) }
                billingService.purchase(BillingProducts.APP_ACCESS, accountId)
            } catch (t: Throwable) {
                error = t.message ?: errorInitPayment
                loading = false
            }
        }
    }

    fun restorePurchase() {
        scope.launch {
            loading = true
            error = null
            try {
                billingService.restore()
            } catch (t: Throwable) {
                error = t.message ?: errorPaymentVerification
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
                            when (result.paymentProvider) {
                                PaymentProvider.APPLE -> {
                                    api.verifyApplePurchase(
                                        VerifyAppleReceiptDto(
                                            transactionId = result.appleTransactionId ?: "",
                                            productId = result.productId
                                        )
                                    )
                                }
                                PaymentProvider.GOOGLE -> {
                                    api.verifyGooglePurchase(
                                        VerifyGoogleReceiptDto(
                                            orderId = result.googleOrderId ?: "",
                                            productId = result.productId,
                                            purchaseToken = result.googlePurchaseToken ?: "",
                                            purchaseData = result.purchaseData ?: "",
                                            signature = result.signature ?: ""
                                        )
                                    )
                                }
                                else -> {}
                            }
                            val paid = api.getPaymentStatus()?.hasAccess == true
                            if (paid) onPaid() else  error = errorPaymentNotConfirmed
                        } catch (t: Throwable) {
                            error = t.message ?: errorPaymentVerification
                        } finally {
                            loading = false
                        }
                    }
                    is PurchaseResult.Failure -> {
                        error = result.message
                        loading = false
                    }
                    is PurchaseResult.NothingToRestore -> {
                        loading = false
                        info = "No previous purchases found to restore."
                    }
                }
            }
        }
        onDispose { job.cancel() }
    }

    // Auto-restore on first entry so users who re-registered after deleting their account
    // get their previous purchase back without needing to find the button.
    LaunchedEffect(Unit) {
        try {
            billingService.connect()
            billingService.restore()
        } catch (t: Throwable) {
            // Restore is best-effort here; the manual button remains available.
            println("Auto-restore skipped: ${t.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.payment_heading),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(Res.string.payment_description, storeName),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(Res.string.premium_access),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        stringResource(Res.string.amount_label, priceLabel),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.secure_payment, storeName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            PrimaryButton(
                text = if (loading) {
                    stringResource(Res.string.opening_payment)
                } else {
                    stringResource(Res.string.pay_button, priceLabel)
                },
                enabled = !loading,
                onClick = { startNativePayment() }
            )

            PrimaryButton(
                text = if (loading) {
                    stringResource(Res.string.processing)
                } else {
                    "Restore Purchase"
                },
                enabled = !loading,
                onClick = { restorePurchase() }
            )

            PrimaryButton(
                text = stringResource(Res.string.logout),
                enabled = !loading,
                onClick = {
                    scope.launch {
                        sessionViewModel.logout()
                        onLogout()
                    }
                }
            )

            if (info != null) {
                Text(
                    text = info!!,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
