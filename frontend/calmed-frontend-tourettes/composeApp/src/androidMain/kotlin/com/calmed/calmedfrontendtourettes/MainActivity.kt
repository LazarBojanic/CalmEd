package com.calmed.calmedfrontendtourettes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.calmed.calmedfrontendtourettes.auth.AppleAuthBridge
import com.calmed.calmedfrontendtourettes.auth.setGoogleAuthActivityProvider
import androidx.lifecycle.lifecycleScope
import com.calmed.calmedfrontendtourettes.billing.BillingProducts
import com.calmed.calmedfrontendtourettes.billing.initBilling
import com.calmed.calmedfrontendtourettes.billing.provideBillingService
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
import com.calmed.calmedfrontendtourettes.notifications.setNotificationPermissionRequester
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject



class MainActivity : ComponentActivity() {
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
			val exists = billing.loadProduct(BillingProducts.PREMIUM_ONE_TIME)
			Log.d("BILLING", "Product exists = $exists (id=${BillingProducts.PREMIUM_ONE_TIME})")
		}
		Log.d("APPLE_AUTH", "onCreate intent=$intent")
		handleAppleDeepLink(intent)
		setGoogleAuthActivityProvider { this }
		setNotificationPermissionRequester { permission ->
			notificationPermissionLauncher.launch(permission)
		}
		appleSignInStarter = { startAppleSignIn() }
		setContent {
			App()
			val context = LocalContext.current

		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		Log.d("APPLE_AUTH", "onNewIntent intent=$intent")
		handleAppleDeepLink(intent)
	}

	private fun handleAppleDeepLink(intent: Intent?) {
		Log.d("APPLE_AUTH", "handleAppleDeepLink CALLED. intent=$intent")

		val data = intent?.data ?: return
		Log.d("APPLE_AUTH", "APPLE CALLBACK URI: $data")

		if (data.scheme != "calmed" || data.host != "apple") return

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
		Log.e("APPLE_AUTH", "START APPLE SIGN IN CALLED")
		val clientId = "com.calmed.auth"
		val redirectUri = "https://api.calm-ed.com/auth/apple/callback"

		val state = java.util.UUID.randomUUID().toString()
		val nonce = java.util.UUID.randomUUID().toString()

		val url = android.net.Uri.Builder()
			.scheme("https")
			.authority("appleid.apple.com")
			.appendPath("auth")
			.appendPath("authorize")
			.appendQueryParameter("response_type", "code id_token")
			.appendQueryParameter("response_mode", "form_post")
			.appendQueryParameter("client_id", clientId)
			.appendQueryParameter("redirect_uri", redirectUri)
			.appendQueryParameter("scope", "name email")
			.appendQueryParameter("state", state)
			.appendQueryParameter("nonce", nonce)
			.build()
		Log.d("APPLE_AUTH", "AUTHORIZE_URL = $url")
		val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
		customTabsIntent.launchUrl(this, url)
	}
}
