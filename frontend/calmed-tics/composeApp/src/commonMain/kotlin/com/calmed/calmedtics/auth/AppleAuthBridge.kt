package com.calmed.calmedtics.auth

object AppleAuthBridge {
    var onAuthCode: ((Result<String>) -> Unit)? = null
    var onIdToken: ((Result<String>) -> Unit)? = null
}
