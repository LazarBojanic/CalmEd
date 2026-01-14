package com.calmed.calmedfrontendtourettes.http

import com.calmed.calmedfrontendtourettes.model.dto.TokenDto
import com.calmed.calmedfrontendtourettes.model.dto.request.ForgotPasswordDto
import com.calmed.calmedfrontendtourettes.model.dto.request.LoginUserDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RefreshDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RegisterUserDto
import com.calmed.calmedfrontendtourettes.model.dto.response.MessageDto
import kotlinx.serialization.Serializable

interface IAppApi {
    suspend fun register(dto: RegisterUserDto): TokenDto?
    suspend fun login(dto: LoginUserDto): TokenDto?
    suspend fun refresh(dto: RefreshDto): TokenDto?
    suspend fun forgotPassword(dto: ForgotPasswordDto): MessageDto?
    suspend fun logout(): MessageDto?
}