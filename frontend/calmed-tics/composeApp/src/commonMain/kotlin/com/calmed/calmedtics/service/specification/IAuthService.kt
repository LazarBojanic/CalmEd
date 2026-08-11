package com.calmed.calmedtics.service.specification

import com.calmed.calmedtics.model.dto.request.SupportMessageRequestDto
import com.calmed.calmedtics.model.dto.response.SupportMessageResponseDto

interface IAuthService {
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(email: String, username: String, password: String, confirmPassword: String, confirmOverEighteen: Boolean): Boolean
    suspend fun forgotPassword(email: String): String?
    suspend fun logout()
    suspend fun tryRefresh(): Boolean
    suspend fun loginWithGoogle(idToken: String): Boolean
    suspend fun loginWithApple(identityToken: String): Boolean
    suspend fun sendSupportMessage(
        request: SupportMessageRequestDto
    ): SupportMessageResponseDto
}