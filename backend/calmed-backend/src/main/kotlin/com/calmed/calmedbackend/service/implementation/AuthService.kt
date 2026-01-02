package com.calmed.calmedbackend.service.implementation

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.auth.JwtConfig
import com.calmed.calmedbackend.auth.TokenType
import com.calmed.calmedbackend.model.AppResult
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

	override suspend fun register(dto: RegisterDto): AppResult<TokenPairDto> {
		if (validatePassword(dto.password, dto.confirmPassword) is AppResult.Success) {
			if (userService.getByEmail(dto.email) is AppResult.Success) {
				val createdUser = userService.create(User.createNew(dto.email, dto.username, false))
				if (createdUser is AppResult.Success) {
					if (authCredentialService.getByUserIdAndType(
							createdUser.data.id,
							AuthCredentialType.BASIC
						) is AppResult.Failure
					) {
						val passwordHash = hashTextBCrypt(dto.password)
						if (passwordHash is AppResult.Success) {
							val createdAuthCredential = authCredentialService.create(
								AuthCredential.createNew(
									createdUser.data.id,
									AuthCredentialType.BASIC,
									passwordHash.data
								)
							)
							if (createdAuthCredential is AppResult.Success) {
								return createTokenPair(createdUser.data.id, createdUser.data.email)
							}
							else {
								return AppResult.Failure("Failed to create auth credential.")
							}
						}
						else {
							return AppResult.Failure("Failed to hash password.")
						}
					}
					else {
						return AppResult.Failure("Auth credential already exists.")
					}
				}
				else {
					return AppResult.Failure("Failed to create user.")
				}
			}
			else {
				return AppResult.Failure("Email already exists.")
			}
		}
		else {
			return AppResult.Failure("Invalid password.")
		}
	}

	override suspend fun login(dto: LoginDto): AppResult<TokenPairDto> {
		val existingUser = userService.getByEmail(dto.email)
		if (existingUser is AppResult.Success) {
			return createTokenPair(existingUser.data.id, existingUser.data.email)
		}
		else {
			return AppResult.Failure("Email does not exist.")
		}
	}

	override suspend fun logout(userId: UUID): AppResult<Unit> {
		return refreshTokenService.revokeAllByUserId(userId, null)
	}

	override suspend fun createTokenPair(
		userId: UUID,
		email: String
	): AppResult<TokenPairDto> {
		val now = Instant.now()
		val access = generateAccessToken(userId, email, now)
		if (access is AppResult.Success) {
			val refresh = generateAndStoreRefreshToken(userId, email, now)
			if (refresh is AppResult.Success) {
				return AppResult.Success(TokenPairDto(access.data, refresh.data))
			}
			else {
				return AppResult.Failure("Failed to generate refresh token.")
			}
		}
		else {
			return AppResult.Failure("Failed to generate access token.")
		}
	}

	override suspend fun generateAccessToken(
		id: UUID,
		email: String,
		now: Instant
	): AppResult<String> {
		return AppResult.Success(
			JWT.create()
				.withIssuer(jwtConfig.iss)
				.withAudience(jwtConfig.aud)
				.withSubject(id.toString())
				.withIssuedAt(now)
				.withExpiresAt(now.plus(jwtConfig.accessTtl))
				.withJWTId(UUID.randomUUID().toString())
				.withClaim("typ", TokenType.ACCESS.name)
				.withClaim("email", email)
				.sign(jwtConfig.algAccess)
		)
	}

	override suspend fun generateAndStoreRefreshToken(
		userId: UUID,
		email: String,
		now: Instant
	): AppResult<String> {
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

		if (jwt != null) {
			val tokenHash = hashTextSHA512(jwt)
			if (tokenHash is AppResult.Success) {
				val createdRefreshToken = refreshTokenService.create(
					RefreshToken(
						id = refreshId,
						replacedBy = null,
						userId = userId,
						tokenHash = tokenHash.data,
						issuedAt = now,
						expiresAt = expires,
						revokedAt = null,
						createdAt = now,
						updatedAt = now
					)
				)
				if (createdRefreshToken is AppResult.Success) {
					return AppResult.Success(jwt)
				}
				else {
					return AppResult.Failure("Failed to create refresh token.")
				}
			}
			else {
				return AppResult.Failure("Failed to create refresh token.")
			}
		}
		else {
			return AppResult.Failure("Failed to create refresh token.")
		}
	}

	override suspend fun refresh(dto: RefreshDto): AppResult<TokenPairDto> {
		TODO()
	}

	override suspend fun validatePassword(
		p: String?,
		c: String?
	): AppResult<Unit> {
		if (p != null && c != null) {
			val validLength = p.length >= 8
			val hasUpper = p.any(Char::isUpperCase)
			val hasLower = p.any(Char::isLowerCase)
			val hasDigit = p.any(Char::isDigit)
			val matches = p == c
			if (validLength && hasUpper && hasLower && hasDigit && matches) {
				return AppResult.Success(Unit)
			}
			else {
				return AppResult.Failure("Invalid password.")
			}
		}
		else {
			return AppResult.Failure("Password must not be null.")
		}
	}

	override suspend fun hashTextBCrypt(text: String?): AppResult<String> {
		if (text != null) {
			return AppResult.Success(
				BCrypt.withDefaults()
					.hashToString(12, text.toCharArray())
			)
		}
		else {
			return AppResult.Failure("Password must not be null.")
		}
	}

	override suspend fun verifyTextBCrypt(
		text: String?,
		hash: String?
	): AppResult<Unit> {
		if (text != null && hash != null) {
			if (BCrypt.verifyer().verify(text.toCharArray(), hash).verified) {
				return AppResult.Success(Unit)
			}
			else {
				return AppResult.Failure("Text not verified.")
			}
		}
		else {
			return AppResult.Failure("Text must not be null.")
		}
	}

	override suspend fun hashTextSHA512(text: String?): AppResult<String> {
		if (text != null) {
			return AppResult.Success(
				MessageDigest.getInstance("SHA-512").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
			)
		}
		else {
			return AppResult.Failure("Text must not be null.")
		}
	}
}
