package com.calmed.calmedbackend.service.specification

import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.auth.TokenType
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.dto.response.TokenPairDto
import com.calmed.calmedbackend.model.raw.user.User
import java.time.Instant
import java.util.UUID

interface IAuthService {
	fun accessVerifier(): JWTVerifier
	fun refreshVerifier(): JWTVerifier
	suspend fun register(registerDto: RegisterDto): TokenPairDto
	suspend fun login(loginDto: LoginDto): TokenPairDto
	suspend fun logout(userId: UUID): Boolean
	suspend fun generateAccessToken(id: UUID, email: String, now: Instant, exp: Instant): String
	suspend fun generateRefreshToken(id: UUID, email: String, now: Instant, exp: Instant): String
	suspend fun hashTextBCrypt(text: String): String
	suspend fun verifyTextBCrypt(text: String, hash: String): Boolean
	suspend fun createTokens(id: UUID, email: String): TokenPairDto
	suspend fun refresh(refreshDto: RefreshDto): TokenPairDto
	suspend fun validatePassword(password: String, confirmPassword: String): Boolean
	suspend fun hashTextSHA512(text: String): String
}