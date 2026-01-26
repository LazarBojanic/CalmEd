package com.calmed.calmedfrontendtourettes.auth

import android.app.Activity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.calmed.calmedfrontendtourettes.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import calmedfrontendtourettes.composeApp.BuildConfig

private var activityProvider: (() -> Activity)? = null

fun setGoogleAuthActivityProvider(provider: () -> Activity) {
	activityProvider = provider
}

private fun extractGoogleIdToken(credential: Credential): String {
	return when (credential) {
		is CustomCredential -> {
			if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
				try {
					GoogleIdTokenCredential.createFrom(credential.data).idToken
				} catch (e: GoogleIdTokenParsingException) {
					throw IllegalStateException("Invalid Google ID token response", e)
				}
			} else {
				throw IllegalStateException("Unexpected credential type: ${credential.type}")
			}
		}
		else -> throw IllegalStateException("Unexpected credential class: ${credential::class.qualifiedName}")
	}
}

private suspend fun getCredential(
	activity: Activity,
	request: GetCredentialRequest
): GetCredentialResponse {
	val cm = CredentialManager.create(activity)
	return cm.getCredential(activity, request)
}

private suspend fun tryReturningUserFlow(activity: Activity): String {
	val googleIdOption = GetGoogleIdOption.Builder()
		.setServerClientId(BuildConfig.googleAndroidClientId)
		.setFilterByAuthorizedAccounts(true)
		.setAutoSelectEnabled(false)
		.build()

	val request = GetCredentialRequest.Builder()
		.addCredentialOption(googleIdOption)
		.build()

	val response = getCredential(activity, request)
	return extractGoogleIdToken(response.credential)
}

private suspend fun buttonInteractiveFlow(activity: Activity): String {
	val option = GetSignInWithGoogleOption.Builder(
		serverClientId = BuildConfig.googleAndroidClientId
	)
		.setNonce("calmed-${System.currentTimeMillis()}")
		.build()

	val request = GetCredentialRequest.Builder()
		.addCredentialOption(option)
		.build()

	val response = getCredential(activity, request)
	return extractGoogleIdToken(response.credential)
}

actual suspend fun getGoogleIdToken(): String {
	val activity = activityProvider?.invoke()
		?: error("GoogleAuth activity provider not set. Call setGoogleAuthActivityProvider() in MainActivity.")

	try {
		return tryReturningUserFlow(activity)
	} catch (t: Throwable) {
	}

	return try {
		buttonInteractiveFlow(activity)
	} catch (e: NoCredentialException) {
		throw IllegalStateException(
			"Credential Manager couldn't find any Google credentials to use. " +
				"On emulator: ensure a Google Play system image, sign into Play Store, and update Google Play services.",
			e
		)
	} catch (e: GetCredentialException) {
		throw e
	}
}