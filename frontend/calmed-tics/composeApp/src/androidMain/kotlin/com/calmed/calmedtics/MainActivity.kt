package com.calmed.calmedtics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import com.calmed.calmedtics.auth.AppleAuthBridge
import com.calmed.calmedtics.auth.setGoogleAuthActivityProvider
import com.calmed.calmedtics.viewmodel.AuthViewModel
import androidx.lifecycle.lifecycleScope
import com.calmed.calmedtics.billing.BillingProducts
import com.calmed.calmedtics.billing.initBilling
import com.calmed.calmedtics.billing.provideBillingService
import com.calmed.calmedtics.notifications.setNotificationPermissionRequester
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.fragment.app.FragmentActivity
import com.calmed.calmedtics.util.setImagePickerActivityProvider

class MainActivity : FragmentActivity() {
	private val authViewModel: AuthViewModel by inject()
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
			val exists = billing.loadProduct(BillingProducts.TEST_APP_ACCESS)
			Log.d("BILLING", "Product exists = $exists (id=${BillingProducts.TEST_APP_ACCESS})")
		}
		Log.d("APPLE_AUTH", "onCreate intent=$intent")
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
		Log.d("APPLE_AUTH", "onNewIntent intent=$intent")
		handleDeepLink(intent)
	}

	private fun handleDeepLink(intent: Intent?) {
		Log.d("APPLE_AUTH", "handleAppleDeepLink CALLED. intent=$intent")

		val data = intent?.data ?: return
		Log.d("APPLE_AUTH", "APPLE CALLBACK URI: $data")

		if (data.scheme != "calmed") return
		if (data.host != "apple") return

		val idToken = data.getQueryParameter("id_token")
		val code = data.getQueryParameter("code")
		val state = data.getQueryParameter("state")
		val error = data.getQueryParameter("error")
		val errorDesc = data.getQueryParameter("error_description")


		if (!error.isNullOrBlank()) {
			Log.e("APPLE_AUTH", "error=$error desc=$errorDesc")
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
			Log.d("APPLE_AUTH", "code=$code")
			AppleAuthBridge.onAuthCode?.invoke(Result.success(code))
		}

		Log.d("APPLE_AUTH", "state=$state")
	}


	private fun startAppleSignIn() {
	}
}
