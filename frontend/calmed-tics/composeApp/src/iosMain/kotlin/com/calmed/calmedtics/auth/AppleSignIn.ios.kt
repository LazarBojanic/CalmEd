package com.calmed.calmedtics.auth

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.AuthenticationServices.*
import kotlin.random.Random
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

private var appleSignInDelegate: AppleSignInDelegate? = null

actual fun launchAppleSignIn() {
    val delegate = AppleSignInDelegate()
    appleSignInDelegate = delegate
    
    try {
        val provider = ASAuthorizationAppleIDProvider()
        val request = provider.createRequest()
        
        request.setRequestedScopes(
            listOf(
                ASAuthorizationScopeFullName,
                ASAuthorizationScopeEmail
            )
        )
        
        request.nonce = generateNonce()
        
        val controller = ASAuthorizationController(listOf(request))
        controller.delegate = delegate
        controller.presentationContextProvider = delegate
        
        controller.performRequests()
        
    } catch (e: Exception) {
        AppleAuthBridge.onIdToken?.invoke(
            Result.failure(IllegalStateException("Apple Sign-In initialization failed: ${e.message}", e))
        )
        appleSignInDelegate = null
    }
}

private fun generateNonce(): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..32).map { characters.random() }.joinToString("")
}

private class AppleSignInDelegate : NSObject(), 
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {
    
    @OptIn(BetaInteropApi::class)
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        try {
            val appleIDCredential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
            if (appleIDCredential != null) {
                val identityToken = appleIDCredential.identityToken
                if (identityToken != null) {
                    val idTokenString = NSString.create(data = identityToken, encoding = NSUTF8StringEncoding).toString()
                    AppleAuthBridge.onIdToken?.invoke(Result.success(idTokenString))
                } else {
                    AppleAuthBridge.onIdToken?.invoke(
                        Result.failure(IllegalStateException("Apple Sign-In succeeded but no ID token received"))
                    )
                }
                
                val authCode = appleIDCredential.authorizationCode
                if (authCode != null) {
                    val authCodeString = NSString.create(data = authCode, encoding = NSUTF8StringEncoding).toString()
                    AppleAuthBridge.onAuthCode?.invoke(Result.success(authCodeString))
                }
            } else {
                AppleAuthBridge.onIdToken?.invoke(
                    Result.failure(IllegalStateException("Apple Sign-In credential is not an Apple ID credential"))
                )
            }
        } catch (e: Exception) {
            AppleAuthBridge.onIdToken?.invoke(
                Result.failure(IllegalStateException("Failed to process Apple Sign-In result: ${e.message}", e))
            )
        } finally {
            appleSignInDelegate = null
        }
    }
    
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        val errorMessage = when {
            didCompleteWithError.domain == ASAuthorizationErrorDomain -> {
                when (didCompleteWithError.code) {
                    ASAuthorizationErrorCanceled -> "Apple Sign-In was cancelled by the user"
                    ASAuthorizationErrorUnknown -> "Apple Sign-In failed: Unknown error"
                    ASAuthorizationErrorInvalidResponse -> "Apple Sign-In failed: Response not successful"
                    ASAuthorizationErrorNotHandled -> "Apple Sign-In failed: Not handled"
                    ASAuthorizationErrorFailed -> "Apple Sign-In failed: Authorization failed"
                    else -> "Apple Sign-In failed: ${didCompleteWithError.localizedDescription}"
                }
            }
            else -> "Apple Sign-In failed: ${didCompleteWithError.localizedDescription}"
        }
        
        AppleAuthBridge.onIdToken?.invoke(
            Result.failure(IllegalStateException(errorMessage))
        )
        appleSignInDelegate = null
    }
    
    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor {
        return UIApplication.sharedApplication.keyWindow ?: 
        UIApplication.sharedApplication.windows.mapNotNull { it as? UIWindow }.firstOrNull { it.isKeyWindow() } ?:
        throw IllegalStateException("No key window available for Apple Sign-In presentation")
    }
}

