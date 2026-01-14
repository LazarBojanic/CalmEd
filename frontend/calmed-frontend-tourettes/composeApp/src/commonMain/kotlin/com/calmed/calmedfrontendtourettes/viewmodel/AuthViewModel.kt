package com.calmed.calmedfrontendtourettes.viewmodel

import com.calmed.calmedfrontendtourettes.service.specification.IAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(
    private val authService: IAuthService
) {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _info = MutableStateFlow<String?>(null)
    val info: StateFlow<String?> = _info

    suspend fun login(email: String, password: String): Boolean {
        _error.value = null
        _info.value = null
        _loading.value = true
        return try {
            val ok = authService.login(email, password)
            if (!ok) _error.value = "Login failed. Check your credentials."
            ok
        } catch (t: Throwable) {
            _error.value = t.message ?: "Login failed."
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun register(email: String, username: String, password: String, confirmPassword: String): Boolean {
        _error.value = null
        _info.value = null
        _loading.value = true
        return try {
            val ok = authService.register(email, username, password, confirmPassword)
            if (!ok) _error.value = "Registration failed. Please review your input."
            ok
        } catch (t: Throwable) {
            _error.value = t.message ?: "Registration failed."
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun forgotPassword(email: String): Boolean {
        _error.value = null
        _info.value = null
        _loading.value = true
        return try {
            val msg = authService.forgotPassword(email)
            // backend intentionally returns OK even if email is unknown
            _info.value = msg ?: "Password reset email sent (if the address exists)."
            true
        } catch (t: Throwable) {
            _error.value = t.message ?: "Request failed."
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun logout() {
        _error.value = null
        _info.value = null
        _loading.value = true
        try {
            authService.logout()
        } finally {
            _loading.value = false
        }
    }
}