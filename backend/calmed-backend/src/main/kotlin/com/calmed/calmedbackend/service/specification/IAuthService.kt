package com.calmed.calmedbackend.service.specification

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.auth.TokenType
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.dto.response.TokenPairDto
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.model.raw.user.User
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

interface IAuthService {
	fun accessVerifier(): JWTVerifier
	fun refreshVerifier(): JWTVerifier

	suspend fun register(dto: RegisterDto): TokenPairDto
	suspend fun login(dto: LoginDto): TokenPairDto
	suspend fun logout(userId: UUID): Boolean
	suspend fun createTokenPair(userId: UUID, email: String): TokenPairDto
	suspend fun generateAccessToken(id: UUID, email: String, now: Instant): String
	suspend fun generateAndStoreRefreshToken(userId: UUID, email: String, now: Instant): String
	suspend fun refresh(dto: RefreshDto): TokenPairDto
	suspend fun validatePassword(p: String, c: String): Boolean
	suspend fun hashTextBCrypt(text: String): String
	suspend fun verifyTextBCrypt(text: String, hash: String): Boolean
	suspend fun hashTextSHA512(text: String): String
}