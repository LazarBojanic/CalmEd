package com.calmed.calmedfrontendtourettes.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import cocoapods.GoogleSignIn.GIDConfiguration
import cocoapods.GoogleSignIn.GIDSignIn
import cocoapods.GoogleSignIn.GIDSignInResult
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import calmedfrontendtourettes.composeApp.BuildConfig
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.Continuation

private var googleSignInDelegate: GoogleSignInDelegate? = null

@OptIn(ExperimentalForeignApi::class)
actual suspend fun getGoogleIdToken(): String {
    return suspendCancellableCoroutine { continuation ->
        val delegate = GoogleSignInDelegate(continuation)
        googleSignInDelegate = delegate
        
        try {
            val config = GIDConfiguration(BuildConfig.googleWebClientId, null)
            
            GIDSignIn.sharedInstance.setConfiguration(config)
            
            // Check if user is already signed in
            if (GIDSignIn.sharedInstance.hasPreviousSignIn()) {
                GIDSignIn.sharedInstance.restorePreviousSignInWithCompletion { user, error ->
                    if (error != null) {
                        // If restore fails, attempt fresh sign-in instead of failing immediately
                        attemptFreshSignIn(delegate)
                    } else {
                        user?.idToken?.tokenString?.let { idToken ->
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

@OptIn(ExperimentalForeignApi::class)
private fun attemptFreshSignIn(delegate: GoogleSignInDelegate) {
    val window = UIApplication.sharedApplication.windows.mapNotNull { it as? UIWindow }.firstOrNull { it.isKeyWindow() }
    val rootViewController = window?.rootViewController

    if (rootViewController == null) {
        delegate.continuation.resumeWithException(IllegalStateException("No root view controller found for Google Sign-In"))
        return
    }

    GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { result, error ->
        if (error != null) {
            delegate.continuation.resumeWithException(
                IllegalStateException("Google Sign-In failed: ${error.localizedDescription}")
            )
        } else {
            result?.user?.idToken?.tokenString?.let { idToken ->
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