package com.calmed.calmedbackend.service.specification

import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.dto.response.TokenPairDto
import java.time.Instant
import java.util.UUID

interface IAuthService {
	fun accessVerifier(): JWTVerifier
	fun refreshVerifier(): JWTVerifier
	fun emailVerificationVerifier(): JWTVerifier
	fun passwordResetVerifier(): JWTVerifier


	suspend fun createTokenPair(userId: UUID, email: String): AppResult<TokenPairDto>
	suspend fun generateAccessToken(id: UUID, email: String, now: Instant): AppResult<String>
	suspend fun generateRefreshToken(userId: UUID, email: String, now: Instant): AppResult<String>
	suspend fun generateEmailVerificationToken(userId: UUID, email: String): AppResult<String>
	suspend fun generatePasswordResetToken(userId: UUID, email: String): AppResult<String>



	suspend fun sendVerificationEmail(userId: UUID, email: String): AppResult<Unit>
	suspend fun verifyEmail(token: String): AppResult<Unit>
	suspend fun resendVerificationEmail(email: String): AppResult<Unit>

	suspend fun sendPasswordResetEmail(email: String): AppResult<Unit>
	suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>

	suspend fun register(dto: RegisterDto): AppResult<TokenPairDto>
	suspend fun login(dto: LoginDto): AppResult<TokenPairDto>
	suspend fun refresh(dto: RefreshDto): AppResult<TokenPairDto>
	suspend fun logout(userId: UUID): AppResult<Unit>
	suspend fun loginWithGoogle(idToken: String): AppResult<TokenPairDto>




	suspend fun validatePassword(p: String?, c: String?): AppResult<Unit>
	suspend fun hashTextBCrypt(text: String?): AppResult<String>
	suspend fun verifyTextBCrypt(text: String?, hash: String?): AppResult<Unit>
	suspend fun hashTextSHA512(text: String?): AppResult<String>
}