package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.error_init_payment
import calmedtics.shared.generated.resources.error_payment_not_confirmed
import calmedtics.shared.generated.resources.error_payment_verification
import calmedtics.shared.generated.resources.nothing_to_restore
import com.calmed.calmedtics.billing.BillingProducts
import com.calmed.calmedtics.billing.BillingService
import com.calmed.calmedtics.billing.PurchaseResult
import com.calmed.calmedtics.billing.obfuscateAccountId
import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedtics.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedtics.model.raw.PaymentProvider
import com.calmed.calmedtics.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class PaymentViewModel(
	private val api: IAppApi,
	private val billingService: BillingService,
	private val sessionRepository: SessionRepository,
) : ViewModel() {

	private val _loading = MutableStateFlow(false)
	val loading: StateFlow<Boolean> = _loading.asStateFlow()

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error.asStateFlow()

	private val _info = MutableStateFlow<String?>(null)
	val info: StateFlow<String?> = _info.asStateFlow()

	private val _priceLabel = MutableStateFlow<String?>(null)
	val priceLabel: StateFlow<String?> = _priceLabel.asStateFlow()

	private val _paid = MutableStateFlow(false)
	val paid: StateFlow<Boolean> = _paid.asStateFlow()

	init {
		viewModelScope.launch {
			billingService.purchaseResults.collectLatest { result ->
				handlePurchaseResult(result)
			}
		}
		viewModelScope.launch {
			try {
				billingService.connect()
				billingService.restore()
				_priceLabel.value = billingService.productPrice(BillingProducts.APP_ACCESS)
			} catch (t: Throwable) {
				// Auto-restore is best-effort; ignore failures.
			}
		}
	}

	fun startNativePayment() {
		viewModelScope.launch {
			_loading.value = true
			_error.value = null
			try {
				val accountId = sessionRepository.user.first()?.email
					?.takeIf { it.isNotBlank() }
					?.let { obfuscateAccountId(it) }
				billingService.purchase(BillingProducts.APP_ACCESS, accountId)
			} catch (t: Throwable) {
				_error.value = t.message ?: getString(Res.string.error_init_payment)
				_loading.value = false
			}
		}
	}

	fun restorePurchase() {
		viewModelScope.launch {
			_loading.value = true
			_error.value = null
			try {
				billingService.restore()
			} catch (t: Throwable) {
				_error.value = t.message ?: getString(Res.string.error_payment_verification)
			} finally {
				_loading.value = false
			}
		}
	}

	private suspend fun handlePurchaseResult(result: PurchaseResult) {
		when (result) {
			is PurchaseResult.Success -> {
				_loading.value = true
				_error.value = null
				try {
					val verifyResult = when (result.paymentProvider) {
						PaymentProvider.APPLE -> api.verifyApplePurchase(
							VerifyAppleReceiptDto(
								transactionId = result.appleTransactionId ?: "",
								productId = result.productId
							)
						)

						PaymentProvider.GOOGLE -> api.verifyGooglePurchase(
							VerifyGoogleReceiptDto(
								orderId = result.googleOrderId ?: "",
								productId = result.productId,
								purchaseToken = result.googlePurchaseToken ?: "",
								purchaseData = result.purchaseData ?: "",
								signature = result.signature ?: ""
							)
						)

						else -> null
					}
					val isStorePurchase =
						result.paymentProvider == PaymentProvider.APPLE ||
							result.paymentProvider == PaymentProvider.GOOGLE
					if (isStorePurchase && verifyResult == null) {
						_error.value = getString(Res.string.error_payment_verification)
					} else if (api.getPaymentStatus()?.hasAccess == true) {
						val token = when (result.paymentProvider) {
							PaymentProvider.APPLE -> result.appleTransactionId ?: ""
							PaymentProvider.GOOGLE -> result.googlePurchaseToken ?: ""
							else -> ""
						}
						if (token.isNotBlank()) {
							runCatching { billingService.completePurchase(token) }
						}
						_paid.value = true
					} else {
						_error.value = getString(Res.string.error_payment_not_confirmed)
					}
				} catch (t: Throwable) {
					_error.value = t.message ?: getString(Res.string.error_payment_verification)
				} finally {
					_loading.value = false
				}
			}

			is PurchaseResult.Failure -> {
				_error.value = result.message
				_loading.value = false
			}

			is PurchaseResult.NothingToRestore -> {
				_loading.value = false
				_info.value = getString(Res.string.nothing_to_restore)
			}
		}
	}
}
