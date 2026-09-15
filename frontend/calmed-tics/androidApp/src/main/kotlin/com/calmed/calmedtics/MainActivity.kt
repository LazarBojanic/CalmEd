package com.calmed.calmedtics

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.calmed.calmedtics.auth.AppleAuthBridge
import com.calmed.calmedtics.auth.AppleAuthStateStore
import com.calmed.calmedtics.auth.setGoogleAuthActivityProvider
import com.calmed.calmedtics.billing.BillingProducts
import com.calmed.calmedtics.billing.BillingService
import com.calmed.calmedtics.di.AndroidActivityHolder
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.notifications.setNotificationPermissionRequester
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class MainActivity : FragmentActivity() {
	companion object {
		var appleSignInStarter: (() -> Unit)? = null
		var appleAuthCodeReceiver: ((String) -> Unit)? = null
	}

	private val authLog = AppLog(LogTags.APPLE_AUTH)
	private val billingLog = AppLog(LogTags.BILLING)
	private val notificationLog = AppLog(LogTags.NOTIFICATIONS)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		AndroidActivityHolder.set(this)
		val notificationPermissionLauncher = registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { granted ->
			notificationLog.debug("POST_NOTIFICATIONS granted=$granted")
		}
		lifecycleScope.launch {
			val billing = GlobalContext.get().get<BillingService>()
			billing.connect()
			val exists = billing.loadProduct(BillingProducts.APP_ACCESS)
			billingLog.debug("Product exists = $exists (id=${BillingProducts.APP_ACCESS})")
		}
		authLog.debug("onCreate")
		handleDeepLink(intent)
		setGoogleAuthActivityProvider { this }
		setNotificationPermissionRequester { permission ->
			notificationPermissionLauncher.launch(permission)
		}
		appleSignInStarter = { startAppleSignIn() }
		setContent {
			App()
		}
	}

	override fun onDestroy() {
		AndroidActivityHolder.clear()
		super.onDestroy()
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		authLog.debug("onNewIntent")
		handleDeepLink(intent)
	}

	private fun handleDeepLink(intent: Intent?) {
		val data = intent?.data ?: return

		if (data.scheme != "calmed") return
		if (data.host != "apple") return

		val expectedState = AppleAuthStateStore.consume(this)
		val returnedState = data.getQueryParameter("state")
		if (expectedState.isNullOrBlank() || returnedState != expectedState) {
			authLog.error("Apple callback state mismatch; rejecting")
			AppleAuthBridge.onIdToken?.invoke(
				Result.failure(IllegalStateException("Apple Sign-In failed: invalid state"))
			)
			return
		}

		val idToken = data.getQueryParameter("id_token")
		val code = data.getQueryParameter("code")
		val error = data.getQueryParameter("error")
		val errorDesc = data.getQueryParameter("error_description")


		if (!error.isNullOrBlank()) {
			authLog.error("Apple sign-in error=$error desc=$errorDesc")
			val errorMessage = errorDesc ?: error
			AppleAuthBridge.onIdToken?.invoke(Result.failure(IllegalStateException("Apple Sign-In failed: $errorMessage")))
			return
		}


		if (!idToken.isNullOrBlank()) {
			authLog.debug("id_token received len=${idToken.length}")
			AppleAuthBridge.onIdToken?.invoke(Result.success(idToken))

			return
		}


		if (!code.isNullOrBlank()) {
			AppleAuthBridge.onAuthCode?.invoke(Result.success(code))
		}
	}


	private fun startAppleSignIn() {
	}
}
