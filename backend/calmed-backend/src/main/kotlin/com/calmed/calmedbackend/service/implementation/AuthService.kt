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
import java.util.Locale
import java.util.Locale.getDefault
import java.util.UUID

class AuthService(
	private val userService: IUserService,
	private val authCredentialService: IAuthCredentialService,
	private val refreshTokenService: IRefreshTokenService,
	private val jwtConfig: JwtConfig
) : IAuthService {
	override fun verifier(): JWTVerifier {
		return JWT.require(jwtConfig.alg)
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.acceptLeeway(2)
			.build()
	}

	override suspend fun register(registerDto: RegisterDto): TokenPairDto {
		require(registerDto.password == registerDto.confirmPassword)
		val existing = userService.getByEmail(registerDto.email)
		require(existing == null)
		val user: User = User.createNew(
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
			val authCredentialJoined = authCredentialService.create(authCredential)
			if (authCredentialJoined != null) {
				return createTokens(userJoined.id, userJoined.email)
			}
		}
		return TokenPairDto("", "")
	}

	override suspend fun login(loginDto: LoginDto): TokenPairDto {
		val userJoined = userService.getByEmail(loginDto.email)
		if (userJoined != null) {
			val authCredential = authCredentialService.getByUserIdAndType(userJoined.id, AuthCredentialType.BASIC)
			if (authCredential != null) {
				val passwordVerified = verifyTextBCrypt(loginDto.password, authCredential.passwordHash)
				if (passwordVerified) {
					return createTokens(userJoined.id, userJoined.email)
				}
			}
		}
		return TokenPairDto("", "")
	}

	override suspend fun logout(userId: UUID): Boolean {
		return refreshTokenService.revokeAllByUserId(userId)
	}

	override suspend fun hash256(text: String): String {
		return MessageDigest.getInstance("SHA-256")
			.digest(text.toByteArray())
			.joinToString("") { "%02x".format(it) }
	}

	override suspend fun hashTextBCrypt(text: String): String {
		require(text.isNotBlank()) { "Text must not be blank" }
		return BCrypt
			.withDefaults()
			.hashToString(12, text.toCharArray())
	}

	override suspend fun verifyTextBCrypt(text: String, hash: String): Boolean {
		require(text.isNotBlank()) { "Text must not be blank" }
		require(hash.isNotBlank()) { "Hash must not be blank" }
		val result = BCrypt.verifyer().verify(text.toCharArray(), hash)
		return result.verified
	}

	override suspend fun generateToken(
		id: UUID,
		email: String,
		tokenType: TokenType,
		now: Instant,
		exp: Instant
	): String {
		return JWT.create()
			.withIssuer(jwtConfig.iss)
			.withAudience(jwtConfig.aud)
			.withSubject(id.toString())
			.withIssuedAt(now)
			.withExpiresAt(exp)
			.withClaim("typ", tokenType.name)
			.withClaim("email", email)
			.sign(Algorithm.HMAC256(jwtConfig.secret))
	}

	override suspend fun createTokens(id: UUID, email: String): TokenPairDto {
		val now = Instant.now()
		val expAccess = now.plus(jwtConfig.accessTtl)
		val expRefresh = now.plus(jwtConfig.refreshTtl)
		val access = generateToken(id, email, TokenType.ACCESS, now, expAccess)
		val refresh = generateToken(id, email, TokenType.REFRESH, now, expRefresh)
		val refreshHash = hash256(refresh)
		val refreshToken = RefreshToken.createNew(
			userId = id,
			tokenHash = refreshHash,
			issuedAt = now,
			expiresAt = expRefresh,
			revokedAt = null,
			createdAt = now,
			updatedAt = now
		)
		val refreshTokenJoined = refreshTokenService.create(refreshToken)
		if (refreshTokenJoined != null) {
			return TokenPairDto(access, refresh)
		}
		return TokenPairDto("", "")
	}

	override suspend fun refresh(refreshDto: RefreshDto): TokenPairDto {
		require(refreshDto.refresh.isNotBlank()) { "Refresh token required" }
		try {
			val decoded = verifier().verify(refreshDto.refresh)
			val tokenType = decoded.getClaim("typ").asString()
			if (tokenType != TokenType.REFRESH.name) {
				return TokenPairDto("", "")
			}
			val expiresAt = decoded.expiresAt.toInstant()
			val now = Instant.now()
			if (expiresAt.isBefore(now)) {
				return TokenPairDto("", "")
			}
			val userId = UUID.fromString(decoded.subject)
			val email = decoded.getClaim("email").asString()
			val refreshHash = hash256(refreshDto.refresh)
			val storedToken = refreshTokenService.getByTokenHash(refreshHash)
			//TODO fix - revoke only those that have expired
			if(storedToken != null) {
				if(storedToken.revokedAt == null && storedToken.expiresAt.isBefore(now)){
					refreshTokenService.revokeAllByUserId(userId)
				}
			}

			return createTokens(userId, email)
		}
		catch (e: Exception) {
			return TokenPairDto("", "")
		}
	}
}