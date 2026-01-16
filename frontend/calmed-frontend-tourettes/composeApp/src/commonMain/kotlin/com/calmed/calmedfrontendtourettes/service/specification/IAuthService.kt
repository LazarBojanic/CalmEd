package com.calmed.calmedfrontendtourettes.service.specification

interface IAuthService {
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(email: String, username: String, password: String, confirmPassword: String): Boolean
    suspend fun forgotPassword(email: String): String?
    suspend fun logout()
    suspend fun tryRefresh(): Boolean
    suspend fun loginWithGoogle(idToken: String): Boolean
}