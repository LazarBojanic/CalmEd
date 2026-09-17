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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.amount_label
import calmedtics.shared.generated.resources.payment_amount_unknown
import calmedtics.shared.generated.resources.pay_button_generic
import calmedtics.shared.generated.resources.restore_purchase
import calmedtics.shared.generated.resources.store_google_play
import calmedtics.shared.generated.resources.store_app_store
import com.calmed.calmedtics.getPlatform
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
import com.calmed.calmedtics.viewmodel.PaymentViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaymentScreen(
    onPaid: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: PaymentViewModel = koinViewModel()
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val error by viewModel.error.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val info by viewModel.info.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val priceLabel by viewModel.priceLabel.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val paid by viewModel.paid.collectAsStateWithLifecycle(LocalLifecycleOwner.current)

    val platformName = remember { getPlatform().name }
    val isAndroid = remember { platformName.startsWith("Android", ignoreCase = true) }
    val storeName = stringResource(if (isAndroid) Res.string.store_google_play else Res.string.store_app_store)

    val fallbackAmount = stringResource(Res.string.payment_amount_unknown)
    val fallbackPay = stringResource(Res.string.pay_button_generic)
    val restoreLabel = stringResource(Res.string.restore_purchase)

    LaunchedEffect(paid) {
        if (paid) onPaid()
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
                        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(Res.string.premium_access),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = priceLabel?.let { stringResource(Res.string.amount_label, it) }
                            ?: fallbackAmount,
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
                    priceLabel?.let { stringResource(Res.string.pay_button, it) } ?: fallbackPay
                },
                enabled = !loading,
                onClick = { viewModel.startNativePayment() }
            )

            PrimaryButton(
                text = if (loading) {
                    stringResource(Res.string.processing)
                } else {
                    restoreLabel
                },
                enabled = !loading,
                onClick = { viewModel.restorePurchase() }
            )

            PrimaryButton(
                text = stringResource(Res.string.logout),
                enabled = !loading,
                onClick = { onLogout() }
            )

            info?.let { infoText ->
                Text(
                    text = infoText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            error?.let { errorText ->
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
