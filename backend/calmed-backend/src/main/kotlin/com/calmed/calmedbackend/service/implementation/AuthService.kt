package com.calmed.calmedbackend.service.implementation

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.auth.JwtConfig
import com.calmed.calmedbackend.auth.TokenType
import com.calmed.calmedbackend.model.dto.request.LoginDto
import com.calmed.calmedbackend.model.dto.request.RefreshDto
import com.calmed.calmedbackend.model.dto.request.RegisterDto
import com.calmed.calmedbackend.model.dto.response.TokenPairDto
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredential
import com.calmed.calmedbackend.model.raw.authcredential.AuthCredentialType
import com.calmed.calmedbackend.model.raw.refreshtoken.RefreshToken
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import com.calmed.calmedbackend.service.specification.IAuthService
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import com.calmed.calmedbackend.service.specification.IUserService
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

class AuthService(
	private val userService: IUserService,
	private val authCredentialService: IAuthCredentialService,
	private val refreshTokenService: IRefreshTokenService,
	private val jwtConfig: JwtConfig
) : IAuthService {
	override fun accessVerifier(): JWTVerifier {
		return JWT.require(jwtConfig.algAccess)
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.acceptLeeway(2)
			.build()
	}

	override fun refreshVerifier(): JWTVerifier {
		return JWT.require(jwtConfig.algRefresh)
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.acceptLeeway(2)
			.build()
	}

	override suspend fun register(registerDto: RegisterDto): TokenPairDto {
		require(validatePassword(registerDto.password, registerDto.confirmPassword))
		val existing = userService.getByEmail(registerDto.email)
		require(existing == null)
		val user = User.createNew(
			email = registerDto.email,
			username = registerDto.username,
			isEmailVerified = false
		)
		val userJoined = userService.create(user)
		if (userJoined != null) {
			val authCredential = AuthCredential.createNew(
				userId = userJoined.id,
				type = AuthCredentialType.BASIC,
				passwordHash = hashTextBCrypt(registerDto.password)
			)

			authCredentialService.create(authCredential)
			return createTokens(userJoined.id, userJoined.email)
		}

		return TokenPairDto("", "")
	}

	override suspend fun login(loginDto: LoginDto): TokenPairDto {
		val userJoined = userService.getByEmail(loginDto.email)
		if (userJoined != null) {
			val authCredential =
				authCredentialService.getByUserIdAndType(userJoined.id, AuthCredentialType.BASIC)

			if (authCredential != null && verifyTextBCrypt(loginDto.password, authCredential.passwordHash)) {
				return createTokens(userJoined.id, userJoined.email)
			}
		}

		return TokenPairDto("", "")
	}

	override suspend fun logout(userId: UUID): Boolean {
		return refreshTokenService.revokeAllByUserId(userId)
	}

	override suspend fun generateAccessToken(
		id: UUID,
		email: String,
		now: Instant,
		exp: Instant
	): String {
		return JWT.create()
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.withSubject(id.toString())
			.withIssuedAt(now)
			.withExpiresAt(exp)
			.withClaim("typ", TokenType.ACCESS.name)
			.withClaim("email", email)
			.withJWTId(UUID.randomUUID().toString())
			.sign(jwtConfig.algAccess)
	}

	override suspend fun generateRefreshToken(
		id: UUID,
		email: String,
		now: Instant,
		exp: Instant
	): String {
		return JWT.create()
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.withSubject(id.toString())
			.withIssuedAt(now)
			.withExpiresAt(exp)
			.withClaim("typ", TokenType.REFRESH.name)
			.withClaim("email", email)
			.withJWTId(UUID.randomUUID().toString())
			.sign(jwtConfig.algRefresh)
	}

	override suspend fun createTokens(id: UUID, email: String): TokenPairDto {
		val now = Instant.now()
		val expAccess = now.plus(jwtConfig.accessTtl)
		val expRefresh = now.plus(jwtConfig.refreshTtl)
		val access = generateAccessToken(id, email, now, expAccess)
		val refresh = generateRefreshToken(id, email, now, expRefresh)
		val refreshHash = hashTextSHA512(refresh)
		val refreshToken = RefreshToken.createNew(
			userId = id,
			tokenHash = refreshHash,
			issuedAt = now,
			expiresAt = expRefresh,
			revokedAt = null
		)
		refreshTokenService.create(refreshToken)

		return TokenPairDto(access, refresh)
	}

	override suspend fun refresh(refreshDto: RefreshDto): TokenPairDto {
		require(refreshDto.refresh.isNotBlank())

		try {
			val decoded = refreshVerifier().verify(refreshDto.refresh)
			val tokenType = decoded.getClaim("typ").asString()
			if (tokenType != TokenType.REFRESH.name) {
				throw IllegalArgumentException("Not a refresh token")
			}
			val now = Instant.now()
			val expiresAt = decoded.expiresAt.toInstant()
			if (expiresAt.isBefore(now)) {
				throw IllegalArgumentException("Refresh token expired")
			}
			val userId = UUID.fromString(decoded.subject)
			val email = decoded.getClaim("email").asString()
			val stored = refreshTokenService.getByTokenHash(hashTextSHA512(refreshDto.refresh))
				?: throw IllegalArgumentException("Refresh token not found or revoked")
			refreshTokenService.revokeByTokenHash(hashTextSHA512(refreshDto.refresh))
			return createTokens(userId, email)
		}
		catch (e: Exception) {
			return TokenPairDto("", "")
		}
	}

	override suspend fun validatePassword(password: String, confirmPassword: String): Boolean {
		val longerThanEight = password.length >= 8
		val hasUppercase = password.any { it.isUpperCase() }
		val hasLowercase = password.any { it.isLowerCase() }
		val hasDigit = password.any { it.isDigit() }
		val match = password == confirmPassword
		return longerThanEight && hasUppercase && hasLowercase && hasDigit && match
	}

	override suspend fun hashTextBCrypt(text: String): String {
		require(text.isNotBlank()) { "Text must not be blank" }
		return BCrypt.withDefaults().hashToString(12, text.toCharArray())
	}

	override suspend fun verifyTextBCrypt(text: String, hash: String): Boolean {
		require(text.isNotBlank()) { "Text must not be blank" }
		require(hash.isNotBlank()) { "Hash must not be blank" }
		val result = BCrypt.verifyer().verify(text.toCharArray(), hash)
		return result.verified
	}

	override suspend fun hashTextSHA512(text: String): String {
		val md = MessageDigest.getInstance("SHA-512")
		val bytes = md.digest(text.toByteArray())
		return bytes.joinToString("") { "%02x".format(it) }
	}
}
