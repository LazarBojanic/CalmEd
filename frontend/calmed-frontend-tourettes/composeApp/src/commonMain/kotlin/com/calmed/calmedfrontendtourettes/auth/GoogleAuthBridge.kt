package com.calmed.calmedfrontendtourettes.auth

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("GoogleAuthBridge")
object GoogleAuthBridge {
    var onIdToken: ((Result<String>) -> Unit)? = null

    fun onIdTokenSuccess(token: String) {
        onIdToken?.invoke(Result.success(token))
    }

    fun onIdTokenFailure(message: String) {
        onIdToken?.invoke(Result.failure(Exception(message)))
    }
}
