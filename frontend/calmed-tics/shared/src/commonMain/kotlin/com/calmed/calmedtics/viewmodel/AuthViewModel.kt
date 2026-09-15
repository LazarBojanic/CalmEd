package com.calmed.calmedtics.viewmodel

import androidx.lifecycle.ViewModel
import com.calmed.calmedtics.logging.AppLog
import com.calmed.calmedtics.logging.LogTags
import com.calmed.calmedtics.service.specification.IAuthService
import calmedtics.shared.generated.resources.Res
import calmedtics.shared.generated.resources.auth_login_failed_credentials
import calmedtics.shared.generated.resources.auth_login_failed
import calmedtics.shared.generated.resources.auth_registration_success
import calmedtics.shared.generated.resources.auth_registration_failed_input
import calmedtics.shared.generated.resources.auth_registration_failed
import calmedtics.shared.generated.resources.auth_reset_email_sent
import calmedtics.shared.generated.resources.auth_request_failed
import calmedtics.shared.generated.resources.auth_google_failed
import calmedtics.shared.generated.resources.auth_apple_failed
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(
    private val authService: IAuthService
) : ViewModel() {

    private val log = AppLog(LogTags.AUTH)

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
                _error.value = getString(Res.string.auth_login_failed_credentials)
                false
            }
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            log.error("Login failed", t)
            _error.value = getString(Res.string.auth_login_failed)
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
                _info.value = getString(Res.string.auth_registration_success)
                true
            } else {
                _error.value = getString(Res.string.auth_registration_failed_input)
                false
            }
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            log.error("Registration failed", t)
            _error.value = getString(Res.string.auth_registration_failed)
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
                _info.value = getString(Res.string.auth_reset_email_sent)
            }
            true
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            log.error("Password reset request failed", t)
            _error.value = getString(Res.string.auth_request_failed)
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
            if (!success) _error.value = getString(Res.string.auth_google_failed)
            success
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            log.error("Google login failed", t)
            _error.value = getString(Res.string.auth_google_failed)
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun loginWithApple(identityToken: String): Boolean {
        _error.value = null
        _info.value = null
        _loading.value = true
        return try {
            val success = authService.loginWithApple(identityToken)
            if (!success) _error.value = getString(Res.string.auth_apple_failed)
            success
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            log.error("Apple login failed", t)
            _error.value = getString(Res.string.auth_apple_failed)
            false
        } finally {
            _loading.value = false
        }
    }
}
