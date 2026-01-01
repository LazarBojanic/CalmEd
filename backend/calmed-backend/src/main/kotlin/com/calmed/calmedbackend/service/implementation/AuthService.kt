package com.calmed.calmedbackend.service.implementation

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
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
import com.calmed.calmedbackend.model.toRaw
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
			.build()
	}

	override fun refreshVerifier(): JWTVerifier {
		return JWT.require(jwtConfig.algRefresh)
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.build()
	}

	override suspend fun register(dto: RegisterDto): TokenPairDto {
		require(validatePassword(dto.password, dto.confirmPassword))
		require(userService.getByEmail(dto.email) == null)
		val user = userService.create(
			User.createNew(dto.email, dto.username, false)
		) ?: error("User creation failed")

		authCredentialService.create(
			AuthCredential.createNew(
				userId = user.id,
				type = AuthCredentialType.BASIC,
				passwordHash = hashTextBCrypt(dto.password)
			)
		)

		return createTokenPair(user.id, user.email)
	}

	override suspend fun login(dto: LoginDto): TokenPairDto {
		val user = userService.getByEmail(dto.email)
			?: error("Invalid credentials")
		val cred = authCredentialService
			.getByUserIdAndType(user.id, AuthCredentialType.BASIC)
			?: error("Invalid credentials")

		require(verifyTextBCrypt(dto.password, cred.passwordHash))

		return createTokenPair(user.id, user.email)
	}

	override suspend fun logout(userId: UUID): Boolean {
		return refreshTokenService.revokeAllByUserId(userId, null)
	}

	override suspend fun createTokenPair(
		userId: UUID,
		email: String
	): TokenPairDto {
		val now = Instant.now()
		val access = generateAccessToken(userId, email, now)
		val refresh = generateAndStoreRefreshToken(userId, email, now)

		return TokenPairDto(access, refresh)
	}

	override suspend fun generateAccessToken(
		id: UUID,
		email: String,
		now: Instant
	): String {
		return JWT.create()
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.withSubject(id.toString())
			.withIssuedAt(now)
			.withExpiresAt(now.plus(jwtConfig.accessTtl))
			.withJWTId(UUID.randomUUID().toString())
			.withClaim("typ", TokenType.ACCESS.name)
			.withClaim("email", email)
			.sign(jwtConfig.algAccess)
	}

	override suspend fun generateAndStoreRefreshToken(
		userId: UUID,
		email: String,
		now: Instant
	): String {
		val refreshId = UUID.randomUUID()
		val expires = now.plus(jwtConfig.refreshTtl)
		val jwt = JWT.create()
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.withSubject(userId.toString())
			.withIssuedAt(now)
			.withExpiresAt(expires)
			.withJWTId(refreshId.toString())
			.withClaim("typ", TokenType.REFRESH.name)
			.withClaim("email", email)
			.sign(jwtConfig.algRefresh)

		refreshTokenService.create(
			RefreshToken.createNew(
				replacedBy = null,
				userId = userId,
				tokenHash = hashTextSHA512(jwt),
				issuedAt = now,
				expiresAt = expires,
				revokedAt = null
			).copy(id = refreshId)
		)

		return jwt
	}

	override suspend fun refresh(dto: RefreshDto): TokenPairDto {
		val now = Instant.now()
		require(dto.refresh.isNotBlank()) { "Refresh token cannot be blank" }
		val decoded = refreshVerifier().verify(dto.refresh)
		val tokenType = decoded.getClaim("typ").asString()
		require(tokenType == TokenType.REFRESH.name) { "Not a refresh token" }
		val refreshId = UUID.fromString(decoded.id)
		val userId = UUID.fromString(decoded.subject)
		val email = decoded.getClaim("email").asString()
		val stored = refreshTokenService.getById(refreshId)
			?: error("Refresh token not found")

		refreshTokenService.checkReuseAndRevoke(stored.toRaw())

		require(stored.isActive()) { "Refresh token is revoked or expired" }
		require(stored.tokenHash == hashTextSHA512(dto.refresh)) { "Refresh token mismatch" }
		val newRefreshId = UUID.randomUUID()
		val newExpires = now.plus(jwtConfig.refreshTtl)
		val newRefreshTokenJwt = JWT.create()
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.withSubject(userId.toString())
			.withIssuedAt(now)
			.withExpiresAt(newExpires)
			.withJWTId(newRefreshId.toString())
			.withClaim("typ", TokenType.REFRESH.name)
			.withClaim("email", email)
			.sign(jwtConfig.algRefresh)
		val newRefreshToken = RefreshToken.createNew(
			replacedBy = null,
			userId = userId,
			tokenHash = hashTextSHA512(newRefreshTokenJwt),
			issuedAt = now,
			expiresAt = newExpires,
			revokedAt = null
		).copy(id = newRefreshId)

		refreshTokenService.create(newRefreshToken)

		refreshTokenService.revokeById(refreshId, newRefreshId)
		val accessToken = generateAccessToken(userId, email, now)

		return TokenPairDto(accessToken, newRefreshTokenJwt)
	}

	override suspend fun validatePassword(
		p: String,
		c: String
	): Boolean {
		val validLength = p.length >= 8
		val hasUpper = p.any(Char::isUpperCase)
		val hasLower = p.any(Char::isLowerCase)
		val hasDigit = p.any(Char::isDigit)
		val matches = p == c

		return validLength && hasUpper && hasLower && hasDigit && matches
	}

	override suspend fun hashTextBCrypt(text: String): String {
		return BCrypt.withDefaults()
			.hashToString(12, text.toCharArray())
	}

	override suspend fun verifyTextBCrypt(
		text: String,
		hash: String
	): Boolean {
		return BCrypt.verifyer()
			.verify(text.toCharArray(), hash)
			.verified
	}

	override suspend fun hashTextSHA512(text: String): String {
		val digest = MessageDigest.getInstance("SHA-512")
			.digest(text.toByteArray())

		return digest.joinToString("") { "%02x".format(it) }
	}
}
