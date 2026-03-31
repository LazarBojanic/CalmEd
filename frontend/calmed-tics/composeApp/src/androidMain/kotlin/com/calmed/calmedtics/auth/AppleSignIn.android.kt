package com.calmed.calmedtics.auth

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.calmed.calmedtics.di.appContext
import java.util.UUID
import calmedtics.composeApp.BuildConfig

actual fun launchAppleSignIn() {
    val context = appContext

    val clientId = BuildConfig.appleWebClientId
    // Use the exact redirect URI registered with Apple. Using localhost/10.0.2.2 causes redirect_uri mismatch or 403 in mobile flows.
    val redirectUri = BuildConfig.appleCallbackURI

    val state = UUID.randomUUID().toString()
    val nonce = UUID.randomUUID().toString()

    val url = Uri.Builder()
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

    val intent = CustomTabsIntent.Builder().build()

    intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    intent.launchUrl(context, url)
}
