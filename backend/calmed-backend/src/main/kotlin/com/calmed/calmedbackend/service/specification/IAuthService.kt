package com.calmed.calmedbackend.service.specification

import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.auth.TokenType
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.dto.response.TokenPairDto
import com.calmed.calmedbackend.model.raw.user.User
import java.time.Instant
import java.util.UUID

interface IAuthService {
	suspend fun verifier(): JWTVerifier
	suspend fun register(registerDto: RegisterDto): TokenPairDto
	suspend fun login(loginDto: LoginDto): TokenPairDto
	suspend fun logout(): Boolean
	suspend fun hash256(text: String): String
	suspend fun generateToken(id: UUID, email: String, tokenType: TokenType, now: Instant): String
	suspend fun hashTextBCrypt(text: String): String
	suspend fun verifyTextBCrypt(text: String, hash: String): Boolean
	suspend fun createTokens(id: UUID, email: String): TokenPairDto
}