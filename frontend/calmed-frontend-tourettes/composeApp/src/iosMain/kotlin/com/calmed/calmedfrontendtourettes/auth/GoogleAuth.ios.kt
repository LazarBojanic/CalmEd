package com.calmed.calmedfrontendtourettes.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.GoogleSignIn.GIDConfiguration
import platform.GoogleSignIn.GIDSignIn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import calmedfrontendtourettes.composeApp.BuildConfig
import kotlin.coroutines.Continuation

private var googleSignInDelegate: GoogleSignInDelegate? = null

actual suspend fun getGoogleIdToken(): String {
    return suspendCancellableCoroutine { continuation ->
        val delegate = GoogleSignInDelegate(continuation)
        googleSignInDelegate = delegate
        
        try {
            val config = GIDConfiguration.clientID(BuildConfig.googleWebClientId)
            
            GIDSignIn.sharedInstance.configuration = config
            
            // Check if user is already signed in
            if (GIDSignIn.sharedInstance.hasPreviousSignIn()) {
                GIDSignIn.sharedInstance.restorePreviousSignIn { error, user ->
                    if (error != null) {
                        continuation.resumeWithException(
                            IllegalStateException("Failed to restore previous Google sign-in: ${error.localizedDescription}")
                        )
                    } else {
                        user?.authentication?.idToken?.let { idToken ->
                            continuation.resume(idToken)
                        } ?: run {
                            // If no valid token, try fresh sign-in
                            attemptFreshSignIn(delegate)
                        }
                    }
                }
            } else {
                // Fresh sign-in
                attemptFreshSignIn(delegate)
            }
        } catch (e: Exception) {
            continuation.resumeWithException(
                IllegalStateException("Google Sign-In configuration error: ${e.message}", e)
            )
        }
        
        continuation.invokeOnCancellation {
            googleSignInDelegate = null
        }
    }
}

private fun attemptFreshSignIn(delegate: GoogleSignInDelegate) {
    GIDSignIn.sharedInstance.signInWithPresenting(null) { result, error in
        if (error != null) {
            delegate.continuation.resumeWithException(
                IllegalStateException("Google Sign-In failed: ${error.localizedDescription}")
            )
        } else {
            result?.user?.authentication?.idToken?.let { idToken ->
                delegate.continuation.resume(idToken)
            } ?: run {
                delegate.continuation.resumeWithException(
                    IllegalStateException("Google Sign-In succeeded but no ID token received")
                )
            }
        }
    }
}

private class GoogleSignInDelegate(
    val continuation: Continuation<String>
)