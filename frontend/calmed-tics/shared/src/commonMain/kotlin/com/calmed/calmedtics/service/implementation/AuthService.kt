package com.calmed.calmedtics.service.implementation

import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.TokenDto
import com.calmed.calmedtics.model.dto.request.AppleLoginDto
import com.calmed.calmedtics.model.dto.request.ForgotPasswordDto
import com.calmed.calmedtics.model.dto.request.GoogleLoginDto
import com.calmed.calmedtics.model.dto.request.LoginUserDto
import com.calmed.calmedtics.model.dto.request.RefreshDto
import com.calmed.calmedtics.model.dto.request.RegisterUserDto
import com.calmed.calmedtics.model.dto.request.SupportMessageRequestDto
import com.calmed.calmedtics.model.dto.response.SupportMessageResponseDto
import com.calmed.calmedtics.service.specification.IAuthService
import com.calmed.calmedtics.store.ITokenDataStore
import io.ktor.client.utils.EmptyContent.contentType

class AuthService(
    private val api: IAppApi,
    private val tokenStore: ITokenDataStore
) : IAuthService {

    override suspend fun login(email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        val loginDto = LoginUserDto(email = trimmedEmail, password = password)
        val token = api.login(loginDto)
        return storeTokenIfValid(token)
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val trimmedEmail = email.trim()
        val trimmedUsername = username.trim()
        val registerDto = RegisterUserDto(
            email = trimmedEmail,
            username = trimmedUsername,
            password = password,
            confirmPassword = confirmPassword
        )
        val token = api.register(registerDto)
        if (token != null) {
            val access = token.access
            val refresh = token.refresh
            if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override suspend fun forgotPassword(email: String): String? {
        val trimmedEmail = email.trim()
        val forgotPasswordDto = ForgotPasswordDto(email = trimmedEmail)
        val response = api.forgotPassword(forgotPasswordDto)
        if (response != null) {
            return response.message
        } else {
            return null
        }
    }

    override suspend fun logout() {
        try {
            api.logout()
        } catch (e: Throwable) {
        } finally {
            tokenStore.clear()
        }
    }

    override suspend fun tryRefresh(): Boolean {
        return try {
            val currentToken = tokenStore.getToken()
            if (currentToken != null) {
                val refresh = currentToken.refresh
                if (refresh != null && refresh.isNotBlank()) {
                    val refreshDto = RefreshDto(refresh = refresh)
                    val newToken = api.refresh(refreshDto)
                    storeTokenIfValid(newToken)
                } else {
                    false
                }
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    private suspend fun storeTokenIfValid(token: TokenDto?): Boolean {
        if (token != null) {
            val access = token.access
            val refresh = token.refresh
            if (access != null && access.isNotBlank() && refresh != null && refresh.isNotBlank()) {
                val tokenDto = TokenDto(access = access, refresh = refresh)
                tokenStore.setToken(tokenDto)
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Boolean {
        println("AUTH: loginWithGoogle() called")
        val token = api.loginWithGoogle(GoogleLoginDto(idToken = idToken))
        println("AUTH: token from api = $token")
        val ok = storeTokenIfValid(token)
        println("AUTH: storeTokenIfValid = $ok")
        return ok
    }

    override suspend fun loginWithApple(identityToken: String): Boolean {
        return try {
            val token = api.loginWithApple(
                dto = AppleLoginDto(identityToken = identityToken)
            )

            println("APPLE_AUTH SERVICE token from backend = $token")

            val ok = storeTokenIfValid(token)
            println("APPLE_AUTH SERVICE storeTokenIfValid = $ok")

            ok
        } catch (e: Exception) {
            println("APPLE_AUTH SERVICE EXCEPTION = ${e.message}")
            false
        }
    }

    override suspend fun sendSupportMessage(
        request: SupportMessageRequestDto
    ): SupportMessageResponseDto {
        return api.sendSupportMessage(request)
    }
}
