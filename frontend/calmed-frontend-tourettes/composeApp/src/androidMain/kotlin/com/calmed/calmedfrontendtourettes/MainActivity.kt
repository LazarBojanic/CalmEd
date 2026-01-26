package com.calmed.calmedfrontendtourettes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.calmed.calmedfrontendtourettes.auth.AppleAuthBridge
import com.calmed.calmedfrontendtourettes.auth.setGoogleAuthActivityProvider
import androidx.lifecycle.lifecycleScope
import com.calmed.calmedfrontendtourettes.viewmodel.AuthViewModel
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
		Log.d("APPLE_AUTH", "onNewIntent: $intent")
		handleAppleDeepLink(intent)
		setGoogleAuthActivityProvider { this }
		appleSignInStarter = { startAppleSignIn() }
		setContent {
			App()
		}
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
			return
		}


		if (!idToken.isNullOrBlank()) {
			Log.d("APPLE_AUTH", "id_token received len=${idToken.length}")

			lifecycleScope.launch {
				Log.d("APPLE_AUTH", "MainActivity CALLING VM loginWithApple")

				val ok = authViewModel.loginWithApple___TEST(code = idToken)

				Log.d("APPLE_AUTH", "MainActivity VM result=$ok")
			}

			return
		}


		if (!code.isNullOrBlank()) {
			Log.d("APPLE_AUTH", "code=$code")
		}

		Log.d("APPLE_AUTH", "state=$state")
	}


	private fun startAppleSignIn() {
		val clientId = "YOUR_APPLE_SERVICES_ID"
		val redirectUri = "https://YOUR_HTTPS_DOMAIN/auth/apple/callback"

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

		val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
		customTabsIntent.launchUrl(this, url)
	}
}