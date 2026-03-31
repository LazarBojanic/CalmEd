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
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.ConfirmPaymentIntentDto
import com.calmed.calmedtics.model.dto.request.CreateCheckoutSessionDto
import com.calmed.calmedtics.model.raw.PaymentType
import com.calmed.calmedtics.payment.StripePaymentResultBridge
import com.calmed.calmedtics.payment.launchStripePaymentSheet
import com.calmed.calmedtics.ui.component.ScreenScaffold
import com.calmed.calmedtics.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PaymentScreen(
    onPaid: () -> Unit,
    sessionViewModel: SessionViewModel = koinInject(),
    api: IAppApi = koinInject()
) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var priceLabel by remember { mutableStateOf("$10.00 (USD)") }

    fun startNativePayment() {
        scope.launch {
            loading = true
            error = null
            try {
                val params = api.createPaymentSheetParams(
                    CreateCheckoutSessionDto(paymentType = PaymentType.CARD)
                )
                if (params == null) {
                    error = "Unable to initialize payment."
                } else {
                    val whole = params.amountCents / 100
                    val cents = params.amountCents % 100
                    val centsText = if (cents < 10) "0$cents" else "$cents"
                    priceLabel = "$$whole.$centsText (${params.currency.uppercase()})"
                    launchStripePaymentSheet(params)
                }
            } catch (t: Throwable) {
                error = t.message ?: "Unable to initialize payment."
            } finally {
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
                if (user?.isPaid == true) onPaid() else error = "Skip payment failed."
            } catch (t: Throwable) {
                error = t.message ?: "Skip payment failed."
            } finally {
                loading = false
            }
        }
    }

    DisposableEffect(Unit) {
        StripePaymentResultBridge.onResult = { success, paymentIntentId, bridgeError ->
            if (!success) {
                error = bridgeError ?: "Payment failed."
            } else if (paymentIntentId.isNullOrBlank()) {
                error = "Missing payment intent id."
            } else {
                scope.launch {
                    loading = true
                    error = null
                    try {
                        api.confirmPaymentIntent(ConfirmPaymentIntentDto(paymentIntentId = paymentIntentId))
                        val user = sessionViewModel.loadSession()
                        if (user?.isPaid == true) onPaid() else error = "Payment not confirmed yet."
                    } catch (t: Throwable) {
                        error = t.message ?: "Payment confirmation failed."
                    } finally {
                        loading = false
                    }
                }
            } 
        }
        onDispose { StripePaymentResultBridge.onResult = null }
    }

    ScreenScaffold(title = "Unlock Premium") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Get full CalmEd access",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "One-time payment. Stripe PaymentSheet will securely show available options on your device.",
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
                        text = "  Premium Program Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Amount: $priceLabel",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, contentDescription = null)
                    Text(
                        text = "  Secure payment via Stripe",
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
                Text(if (loading) "Opening payment..." else "Pay $10.00")
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
                Text(if (loading) "Processing..." else "Skip Payment (Dev Only)")
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
