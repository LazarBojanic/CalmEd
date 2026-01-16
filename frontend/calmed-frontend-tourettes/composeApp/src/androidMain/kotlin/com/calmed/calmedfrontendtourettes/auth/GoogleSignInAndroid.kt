package com.calmed.calmedfrontendtourettes.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.calmed.calmedfrontendtourettes.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential


private var activityProvider: (() -> Activity)? = null

fun setGoogleAuthActivityProvider(provider: () -> Activity) {
    activityProvider = provider
}


suspend fun getGoogleIdTokenAndroid(activity: Activity): String {
    val credentialManager = CredentialManager.create(activity)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(activity.getString(R.string.google_server_client_id))
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .setNonce("calmed-${System.currentTimeMillis()}")
        .build()


    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = credentialManager.getCredential(activity, request)
    val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

    return googleCredential.idToken
}


actual suspend fun getGoogleIdToken(): String {
    val activity = activityProvider?.invoke()
        ?: error("GoogleAuth activity provider not set. Call setGoogleAuthActivityProvider() in MainActivity.")
    return getGoogleIdTokenAndroid(activity)
}
