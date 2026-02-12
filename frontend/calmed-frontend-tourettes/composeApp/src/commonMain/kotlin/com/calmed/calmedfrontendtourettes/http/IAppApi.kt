package com.calmed.calmedfrontendtourettes.http

import com.calmed.calmedfrontendtourettes.model.dto.TokenDto
import com.calmed.calmedfrontendtourettes.model.dto.request.AppleLoginDto
import com.calmed.calmedfrontendtourettes.model.dto.request.ForgotPasswordDto
import com.calmed.calmedfrontendtourettes.model.dto.request.GoogleLoginDto
import com.calmed.calmedfrontendtourettes.model.dto.request.LoginUserDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RefreshDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RegisterUserDto
import com.calmed.calmedfrontendtourettes.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedfrontendtourettes.model.dto.request.UserInfoTourettesUpdateDto
import com.calmed.calmedfrontendtourettes.model.dto.response.HomeDto
import com.calmed.calmedfrontendtourettes.model.dto.response.MessageDto
import com.calmed.calmedfrontendtourettes.model.dto.response.UserDto
import com.calmed.calmedfrontendtourettes.model.dto.response.UserInfoTourettesDto


interface IAppApi {
    suspend fun register(dto: RegisterUserDto): TokenDto?
    suspend fun login(dto: LoginUserDto): TokenDto?
    suspend fun refresh(dto: RefreshDto): TokenDto?
    suspend fun forgotPassword(dto: ForgotPasswordDto): MessageDto?
    suspend fun logout(): MessageDto?
    suspend fun loginWithGoogle(dto: GoogleLoginDto): TokenDto?
    suspend fun ping(): String

    suspend fun getUser(id: String): UserDto?
    suspend fun setOnboarded(id: String, dto: SetIsOnboardedDto): UserDto?

    suspend fun getUserInfoTourettesByUserId(userId: String): UserInfoTourettesDto?
    suspend fun updateUserInfoTourettes(id: String, dto: UserInfoTourettesUpdateDto): UserInfoTourettesDto?
    suspend fun loginWithApple(dto: AppleLoginDto): TokenDto?
    suspend fun getHome(year: Int, month: Int): HomeDto?

}