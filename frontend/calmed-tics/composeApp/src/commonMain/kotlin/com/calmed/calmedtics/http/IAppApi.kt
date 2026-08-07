package com.calmed.calmedtics.http

import com.calmed.calmedtics.model.dto.TokenDto
import com.calmed.calmedtics.model.dto.request.AppleLoginDto
import com.calmed.calmedtics.model.dto.request.ForgotPasswordDto
import com.calmed.calmedtics.model.dto.request.GoogleLoginDto
import com.calmed.calmedtics.model.dto.request.LoginUserDto
import com.calmed.calmedtics.model.dto.request.RefreshDto
import com.calmed.calmedtics.model.dto.request.RegisterUserDto
import com.calmed.calmedtics.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedtics.model.dto.request.SupportMessageRequestDto
import com.calmed.calmedtics.model.dto.request.UserExerciseProgressUpdateDto
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedtics.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedtics.model.dto.response.HomeDto
import com.calmed.calmedtics.model.dto.response.MessageDto
import com.calmed.calmedtics.model.dto.response.PaymentStatusDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.model.dto.response.SupportMessageResponseDto
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
    suspend fun uploadProfileImage(
        imageBytes: ByteArray,
        fileName: String = "profile.jpg"
    ): UserDto
    suspend fun setOnboarded(id: String, dto: SetIsOnboardedDto): UserDto?
    suspend fun syncExerciseProgress(dto: UserExerciseProgressUpdateDto): Boolean

    suspend fun getUserInfoTicsByUserId(userId: String): UserInfoTicsDto?
    suspend fun updateUserInfoTics(id: String, dto: UserInfoTicsUpdateDto): UserInfoTicsDto?
    suspend fun loginWithApple(dto: AppleLoginDto): TokenDto?
    suspend fun getHome(year: Int, month: Int): HomeDto?
    suspend fun getAllProgramExercises(): List<ProgramExerciseDto>
    suspend fun getWelcomeVideo(): ProgramExerciseDto?
    suspend fun getCourseOverviewVideo(): ProgramExerciseDto?
    suspend fun getPaymentStatus(): PaymentStatusDto?
    suspend fun skipPayment(): PaymentStatusDto?
    suspend fun sendSupportMessage(
        request: SupportMessageRequestDto
    ): SupportMessageResponseDto
    suspend fun verifyApplePurchase(dto: VerifyAppleReceiptDto): PaymentStatusDto?
    suspend fun verifyGooglePurchase(dto: VerifyGoogleReceiptDto): PaymentStatusDto?
}
