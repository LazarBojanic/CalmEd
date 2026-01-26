package com.calmed.calmedfrontendtourettes.auth

object AppleAuthBridge {
    var onAuthCode: ((String) -> Unit)? = null
    var onIdToken: ((String) -> Unit)? = null
}
