package com.calmed.calmedfrontendtourettes.auth

actual suspend fun getGoogleIdToken(): String {
    throw NotImplementedError("iOS Google Sign-In not implemented yet (Mac dev task).")
}