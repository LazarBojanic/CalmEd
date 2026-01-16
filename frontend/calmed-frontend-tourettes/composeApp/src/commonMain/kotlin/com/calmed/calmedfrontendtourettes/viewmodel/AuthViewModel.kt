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
            val success = authService.login(email, password)
            if (success) {
                true
            } else {
                _error.value = "Login failed. Check your credentials."
                false
            }
        } catch (t: Throwable) {
            val errorMessage = t.message
            if (errorMessage != null) {
                _error.value = errorMessage
            } else {
                _error.value = "Login failed."
            }
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
            val success = authService.register(email, username, password, confirmPassword)
            if (success) {
                _info.value = "Registration successful! Please check your email to verify your account before logging in."
                true
            } else {
                _error.value = "Registration failed. Please review your input."
                false
            }
        } catch (t: Throwable) {
            val errorMessage = t.message
            if (errorMessage != null) {
                _error.value = errorMessage
            } else {
                _error.value = "Registration failed."
            }
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
            val message = authService.forgotPassword(email)
            if (message != null) {
                _info.value = message
            } else {
                _info.value = "Password reset email sent (if the address exists)."
            }
            true
        } catch (t: Throwable) {
            val errorMessage = t.message
            if (errorMessage != null) {
                _error.value = errorMessage
            } else {
                _error.value = "Request failed."
            }
            false
        } finally {
            _loading.value = false
        }
    }
    suspend fun loginWithGoogle(idToken: String): Boolean {
        _error.value = null
        _info.value = null
        _loading.value = true
        return try {
            val success = authService.loginWithGoogle(idToken)
            if (!success) _error.value = "Google login failed."
            success
        } catch (t: Throwable) {
            _error.value = t.message ?: "Google login failed."
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