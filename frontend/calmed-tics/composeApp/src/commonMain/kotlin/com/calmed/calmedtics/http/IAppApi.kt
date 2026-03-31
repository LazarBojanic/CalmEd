package com.calmed.calmedtics.http

import com.calmed.calmedtics.model.dto.TokenDto
import com.calmed.calmedtics.model.dto.request.AppleLoginDto
import com.calmed.calmedtics.model.dto.request.ConfirmPaymentIntentDto
import com.calmed.calmedtics.model.dto.request.CreateCheckoutSessionDto
import com.calmed.calmedtics.model.dto.request.ForgotPasswordDto
import com.calmed.calmedtics.model.dto.request.GoogleLoginDto
import com.calmed.calmedtics.model.dto.request.LoginUserDto
import com.calmed.calmedtics.model.dto.request.RefreshDto
import com.calmed.calmedtics.model.dto.request.RegisterUserDto
import com.calmed.calmedtics.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.dto.response.HomeDto
import com.calmed.calmedtics.model.dto.response.MessageDto
import com.calmed.calmedtics.model.dto.response.PaymentStatusDto
import com.calmed.calmedtics.model.dto.response.PaymentSheetParamsDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.model.dto.response.UserDto
import com.calmed.calmedtics.model.dto.response.UserInfoTicsDto


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

    suspend fun getUserInfoTicsByUserId(userId: String): UserInfoTicsDto?
    suspend fun updateUserInfoTics(id: String, dto: UserInfoTicsUpdateDto): UserInfoTicsDto?
    suspend fun loginWithApple(dto: AppleLoginDto): TokenDto?
    suspend fun getHome(year: Int, month: Int): HomeDto?
    suspend fun getAllProgramExercises(): List<ProgramExerciseDto>
    suspend fun getWelcomeVideo(): ProgramExerciseDto?
    suspend fun getPaymentStatus(): PaymentStatusDto?
    suspend fun createPaymentSheetParams(dto: CreateCheckoutSessionDto): PaymentSheetParamsDto?
    suspend fun confirmPaymentIntent(dto: ConfirmPaymentIntentDto): PaymentStatusDto?
    suspend fun skipPayment(): PaymentStatusDto?
}
