package com.calmed.calmedfrontendtourettes.auth

import android.app.Activity
import android.content.Intent
import com.calmed.calmedfrontendtourettes.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

fun buildGoogleSignInIntent(activity: Activity): Intent {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(activity.getString(R.string.google_server_client_id))
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(activity, gso).signInIntent
}

fun extractIdTokenFromResult(data: Intent?): String {
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    val account = task.getResult(ApiException::class.java)
    return account.idToken ?: error("Google ID token is null")
}