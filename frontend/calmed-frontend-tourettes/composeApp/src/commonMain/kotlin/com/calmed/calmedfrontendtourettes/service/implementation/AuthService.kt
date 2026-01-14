package com.calmed.calmedfrontendtourettes.service.implementation

import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.model.dto.TokenDto
import com.calmed.calmedfrontendtourettes.model.dto.request.ForgotPasswordDto
import com.calmed.calmedfrontendtourettes.model.dto.request.LoginUserDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RefreshDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RegisterUserDto
import com.calmed.calmedfrontendtourettes.service.specification.IAuthService
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore

class AuthService(
    private val api: IAppApi,
    private val tokenStore: ITokenDataStore,
) : IAuthService {

    override suspend fun login(email: String, password: String): Boolean {
        val token = api.login(LoginUserDto(email = email.trim(), password = password))
        return storeIfValid(token)
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val token = api.register(
            RegisterUserDto(
                email = email.trim(),
                username = username.trim(),
                password = password,
                confirmPassword = confirmPassword
            )
        )
        return storeIfValid(token)
    }

    override suspend fun forgotPassword(email: String): String? {
        return api.forgotPassword(ForgotPasswordDto(email = email.trim()))?.message
    }

    override suspend fun logout() {
        runCatching { api.logout() }
        tokenStore.clear()
    }

    override suspend fun tryRefresh(): Boolean {
        val refresh = tokenStore.tokenDto.value?.refresh?.takeIf { it.isNotBlank() } ?: return false
        val token = api.refresh(RefreshDto(refresh = refresh))
        return storeIfValid(token)
    }

    private suspend fun storeIfValid(token: TokenDto?): Boolean {
        val access = token?.access?.takeIf { !it.isNullOrBlank() }
        val refresh = token?.refresh?.takeIf { !it.isNullOrBlank() }
        return if (access != null && refresh != null) {
            tokenStore.setToken(TokenDto(access = access, refresh = refresh))
            true
        } else {
            false
        }
    }
}