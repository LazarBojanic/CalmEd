package com.calmed.calmedfrontendtourettes.auth

object AppleAuthBridge {
    var onAuthCode: ((Result<String>) -> Unit)? = null
    var onIdToken: ((Result<String>) -> Unit)? = null
}
