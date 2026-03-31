package com.calmed.calmedtics.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.Foundation.NSNotificationCenter

actual suspend fun getGoogleIdToken(): String {
    return suspendCancellableCoroutine { continuation ->
        GoogleAuthBridge.onIdToken = { result ->
            result.fold(
                onSuccess = { token -> continuation.resume(token) },
                onFailure = { error -> continuation.resumeWithException(error) }
            )
            GoogleAuthBridge.onIdToken = null
        }
        
        NSNotificationCenter.defaultCenter.postNotificationName("TriggerGoogleSignIn", null)
        
        continuation.invokeOnCancellation {
            GoogleAuthBridge.onIdToken = null
        }
    }
}