package com.calmed.calmedtics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.calmed.calmedtics.auth.AppleAuthBridge
import com.calmed.calmedtics.auth.AppleAuthStateStore
import com.calmed.calmedtics.auth.setGoogleAuthActivityProvider
import com.calmed.calmedtics.billing.BillingProducts
import com.calmed.calmedtics.billing.initBilling
import com.calmed.calmedtics.billing.provideBillingService
import com.calmed.calmedtics.notifications.setNotificationPermissionRequester
import com.calmed.calmedtics.util.setImagePickerActivityProvider
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
	companion object {
		var appleSignInStarter: (() -> Unit)? = null
		var appleAuthCodeReceiver: ((String) -> Unit)? = null
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val notificationPermissionLauncher = registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { granted ->
			Log.d("NOTIFICATIONS", "POST_NOTIFICATIONS granted=$granted")
		}
		initBilling(this)
		lifecycleScope.launch {
			val billing = provideBillingService()
			billing.connect()
			val exists = billing.loadProduct(BillingProducts.APP_ACCESS)
			Log.d("BILLING", "Product exists = $exists (id=${BillingProducts.APP_ACCESS})")
		}
		Log.d("APPLE_AUTH", "onCreate")
		handleDeepLink(intent)
		setGoogleAuthActivityProvider { this }
		setImagePickerActivityProvider { this }
		setNotificationPermissionRequester { permission ->
			notificationPermissionLauncher.launch(permission)
		}
		appleSignInStarter = { startAppleSignIn() }
		setContent {
			App()
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		Log.d("APPLE_AUTH", "onNewIntent")
		handleDeepLink(intent)
	}

	private fun handleDeepLink(intent: Intent?) {
		val data = intent?.data ?: return

		if (data.scheme != "calmed") return
		if (data.host != "apple") return

		val expectedState = AppleAuthStateStore.consume(this)
		val returnedState = data.getQueryParameter("state")
		if (expectedState.isNullOrBlank() || returnedState != expectedState) {
			Log.e("APPLE_AUTH", "Apple callback state mismatch; rejecting")
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
			Log.e("APPLE_AUTH", "Apple sign-in error=$error desc=$errorDesc")
			val errorMessage = errorDesc ?: error
			AppleAuthBridge.onIdToken?.invoke(Result.failure(IllegalStateException("Apple Sign-In failed: $errorMessage")))
			return
		}


		if (!idToken.isNullOrBlank()) {
			Log.d("APPLE_AUTH", "id_token received len=${idToken.length}")
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
