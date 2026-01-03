package com.calmed.calmedbackend.service.implementation

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.auth.TokenType
import com.calmed.calmedbackend.config.EmailConfig
import com.calmed.calmedbackend.config.JwtConfig
import com.calmed.calmedbackend.database.withTransaction
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
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.UUID

class AuthService(
	private val userService: IUserService,
	private val authCredentialService: IAuthCredentialService,
	private val refreshTokenService: IRefreshTokenService,
	private val jwtConfig: JwtConfig,
	private val emailConfig: EmailConfig
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
		return withTransaction {
			// 1. Validate password
			val passwordValidation = validatePassword(dto.password, dto.confirmPassword)
			if (passwordValidation is AppResult.Success) {
				// 2. Check if email already exists
				val existingUser = userService.getByEmail(dto.email)
				if (existingUser is AppResult.Failure) {
					// 3. Create user
					val newUser = User.createNew(
						email = dto.email,
						username = dto.username,
						isEmailVerified = false
					)
					val createdUser = userService.create(newUser)
					if (createdUser is AppResult.Success) {
						// 4. Hash password
						val passwordHash = hashTextBCrypt(dto.password)
						if (passwordHash is AppResult.Success) {
							// 5. Create auth credential
							val authCredential = AuthCredential.createNew(
								userId = createdUser.data.id,
								type = AuthCredentialType.BASIC,
								passwordHash = passwordHash.data
							)
							val createdAuthCredential = authCredentialService.create(authCredential)
							if (createdAuthCredential is AppResult.Success) {
								// 6. Send verification email (async - don't block registration)
								CoroutineScope(Dispatchers.IO).launch {
									val emailSentResult = sendVerificationEmail(
										createdUser.data.id,
										createdUser.data.email
									)
									if (emailSentResult is AppResult.Success
									) {
										println("Email sent to ${createdUser.data.email}")
									}
									else if (emailSentResult is AppResult.Failure) {
										println(emailSentResult.message)
									}
								}
								// 7. Generate tokens
								return@withTransaction createTokenPair(createdUser.data.id, createdUser.data.email)
							}
							else {
								return@withTransaction AppResult.Failure(
									HttpStatusCode.InternalServerError,
									"Failed to create authentication credentials."
								)
							}
						}
						else {
							return@withTransaction AppResult.Failure(
								HttpStatusCode.InternalServerError,
								"Failed to hash password."
							)
						}
					}
					else {
						return@withTransaction AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to create user.")
					}
				}
				else {
					return@withTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Email already exists.")
				}
			}
			else {
				return@withTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Invalid password.")
			}
		}
	}

	override suspend fun login(dto: LoginDto): AppResult<TokenPairDto> {
		return withTransaction {
			// 1. Find user by email
			val userResult = userService.getByEmail(dto.email)
			if (userResult is AppResult.Success) {
				// 2. Get auth credentials for BASIC authentication
				if (userResult.data.isEmailVerified) {
					val authCredentialResult = authCredentialService.getByUserIdAndType(
						userResult.data.id,
						AuthCredentialType.BASIC
					)

					if (authCredentialResult is AppResult.Success) {
						// 3. Verify password
						val verifyResult = verifyTextBCrypt(
							dto.password,
							authCredentialResult.data.passwordHash
						)

						if (verifyResult is AppResult.Success) {
							// 4. Generate tokens
							return@withTransaction createTokenPair(userResult.data.id, userResult.data.email)
						}
						else {
							return@withTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Invalid password.")
						}
					}
					else {
						return@withTransaction AppResult.Failure(
							HttpStatusCode.InternalServerError,
							"No authentication credentials found."
						)
					}
				}
				else {
					return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Email not verified.")
				}
			}
			else {
				return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Invalid email.")
			}
		}
	}

	override suspend fun logout(userId: UUID): AppResult<Unit> {
		return withTransaction {
			refreshTokenService.revokeAllByUserId(userId, null)
		}
	}

	override suspend fun createTokenPair(
		userId: UUID,
		email: String
	): AppResult<TokenPairDto> {
		return withTransaction {
			val now = Instant.now()
			// 1. Generate access token
			val accessTokenResult = generateAccessToken(userId, email, now)
			if (accessTokenResult is AppResult.Success) {
				// 2. Generate refresh token (JWT only, not stored yet)
				val refreshTokenResult = generateRefreshToken(userId, email, now)
				if (refreshTokenResult is AppResult.Success) {
					val refreshTokenJwt = refreshTokenResult.data
					// 3. Hash the refresh token for storage
					val tokenHash = hashTextSHA512(refreshTokenJwt)
					if (tokenHash is AppResult.Success) {
						// 4. Decode refresh token to get its ID (jti claim)
						val decodedRefreshToken = try {
							refreshVerifier().verify(refreshTokenJwt)
						}
						catch (e: JWTVerificationException) {
							return@withTransaction AppResult.Failure(
								HttpStatusCode.NotFound,
								"Failed to decode refresh token."
							)
						}
						val refreshTokenId = UUID.fromString(decodedRefreshToken.id)
						// 5. Create and store refresh token entity
						val refreshToken = RefreshToken.createNew(
							id = refreshTokenId,
							replacedBy = null,
							userId = userId,
							tokenHash = tokenHash.data,
							issuedAt = now,
							expiresAt = now.plus(jwtConfig.refreshTtl),
							revokedAt = null
						)
						val storedRefreshToken = refreshTokenService.create(refreshToken)
						if (storedRefreshToken is AppResult.Success) {
							return@withTransaction AppResult.Success(
								TokenPairDto(
									access = accessTokenResult.data,
									refresh = refreshTokenJwt
								)
							)
						}
						else {
							return@withTransaction AppResult.Failure(
								HttpStatusCode.NotFound,
								"Failed to store refresh token."
							)
						}
					}
					else {
						return@withTransaction AppResult.Failure(
							HttpStatusCode.NotFound,
							"Failed to hash refresh token."
						)
					}
				}
				else {
					return@withTransaction AppResult.Failure(
						HttpStatusCode.NotFound,
						"Failed to generate refresh token."
					)
				}
			}
			else {
				return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to generate access token.")
			}
		}
	}

	override suspend fun generateAccessToken(
		id: UUID,
		email: String,
		now: Instant
	): AppResult<String> {
		return try {
			val token = JWT.create()
				.withIssuer(jwtConfig.iss)
				.withAudience(jwtConfig.aud)
				.withSubject(id.toString())
				.withIssuedAt(now)
				.withExpiresAt(now.plus(jwtConfig.accessTtl))
				.withJWTId(UUID.randomUUID().toString())
				.withClaim("typ", TokenType.ACCESS.name)
				.withClaim("email", email)
				.sign(jwtConfig.algAccess)

			AppResult.Success(token)
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.NotFound, "Failed to generate access token: ${e.message}.")
		}
	}

	override suspend fun generateRefreshToken(
		userId: UUID,
		email: String,
		now: Instant
	): AppResult<String> {
		return try {
			val refreshTokenId = UUID.randomUUID()
			val expiresAt = now.plus(jwtConfig.refreshTtl)
			val token = JWT.create()
				.withIssuer(jwtConfig.iss)
				.withAudience(jwtConfig.aud)
				.withSubject(userId.toString())
				.withIssuedAt(now)
				.withExpiresAt(expiresAt)
				.withJWTId(refreshTokenId.toString())
				.withClaim("typ", TokenType.REFRESH.name)
				.withClaim("email", email)
				.sign(jwtConfig.algRefresh)

			AppResult.Success(token)
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.NotFound, "Failed to generate refresh token: ${e.message}.")
		}
	}

	override suspend fun refresh(dto: RefreshDto): AppResult<TokenPairDto> {
		return withTransaction {
			// 1. Verify and decode the refresh token
			val decodedRefreshToken = try {
				refreshVerifier().verify(dto.refresh)
			}
			catch (e: JWTVerificationException) {
				return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Invalid refresh token.")
			}
			// 2. Validate token type
			val tokenType = decodedRefreshToken.getClaim("typ").asString()
			if (tokenType == TokenType.REFRESH.name) {
				// 3. Extract claims
				val tokenId = decodedRefreshToken.id
				val userId = decodedRefreshToken.subject
				val email = decodedRefreshToken.getClaim("email").asString()

				if (tokenId != null && userId != null && email != null) {
					val refreshTokenUuid = UUID.fromString(tokenId)
					val userUuid = UUID.fromString(userId)
					// 4. Find stored refresh token
					val storedTokenResult = refreshTokenService.getById(refreshTokenUuid)
					if (storedTokenResult is AppResult.Success) {
						val storedToken = storedTokenResult.data.toRaw()
						// 5. Verify token is active
						if (storedToken.isActive()) {
							// 6. Verify token hash matches
							val incomingHash = hashTextSHA512(dto.refresh)
							if (incomingHash is AppResult.Success) {
								if (incomingHash.data == storedToken.tokenHash) {
									// 7. Generate new token pair
									val newTokenPair = createTokenPair(userUuid, email)

									if (newTokenPair is AppResult.Success) {
										// 8. Decode new refresh token to get its ID
										val newDecodedToken = try {
											refreshVerifier().verify(newTokenPair.data.refresh)
										}
										catch (e: JWTVerificationException) {
											return@withTransaction AppResult.Failure(
												HttpStatusCode.NotFound,
												"Failed to decode new refresh token"
											)
										}
										val newTokenId = UUID.fromString(newDecodedToken.id)
										// 9. Revoke old token (mark as replaced)
										val revokedToken = storedToken.copy(
											replacedBy = newTokenId,
											revokedAt = Instant.now(),
											updatedAt = Instant.now()
										)
										val updateResult = refreshTokenService.update(revokedToken)
										if (updateResult is AppResult.Success) {
											return@withTransaction newTokenPair
										}
										else {
											return@withTransaction AppResult.Failure(
												HttpStatusCode.NotFound,
												"Failed to revoke old refresh token."
											)
										}
									}
									else {
										return@withTransaction newTokenPair
									}
								}
								else {
									return@withTransaction AppResult.Failure(
										HttpStatusCode.NotFound,
										"Token hash mismatch."
									)
								}
							}
							else {
								return@withTransaction AppResult.Failure(
									HttpStatusCode.NotFound,
									"Failed to hash incoming token."
								)
							}
						}
						else {
							return@withTransaction AppResult.Failure(
								HttpStatusCode.NotFound,
								"Refresh token is no longer active."
							)
						}
					}
					else {
						return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Refresh token not found.")
					}
				}
				else {
					return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Invalid token claims.")
				}
			}
			else {
				return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Invalid token type.")
			}
		}
	}

	override suspend fun validatePassword(
		p: String?,
		c: String?
	): AppResult<Unit> {
		if (p != null && c != null) {
			if (p.length >= 8) {
				if (p.any(Char::isUpperCase)) {
					if (p.any(Char::isLowerCase)) {
						if (p.any(Char::isDigit)) {
							if (p == c) {
								return AppResult.Success(Unit)
							}
							else {
								return AppResult.Failure(HttpStatusCode.NotFound, "Passwords do not match.")
							}
						}
						else {
							return AppResult.Failure(
								HttpStatusCode.NotFound,
								"Password must contain at least one digit."
							)
						}
					}
					else {
						return AppResult.Failure(
							HttpStatusCode.NotFound,
							"Password must contain at least one lowercase letter."
						)
					}
				}
				else {
					return AppResult.Failure(
						HttpStatusCode.NotFound,
						"Password must contain at least one uppercase letter."
					)
				}
			}
			else {
				return AppResult.Failure(HttpStatusCode.NotFound, "Password must be at least 8 characters long.")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "Password must not be null.")
		}
	}

	override suspend fun hashTextBCrypt(text: String?): AppResult<String> {
		if (text != null) {
			return try {
				val hash = BCrypt.withDefaults()
					.hashToString(12, text.toCharArray())
				AppResult.Success(hash)
			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.NotFound, "Failed to hash text: ${e.message}.")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "Text must not be null.")
		}
	}

	override suspend fun verifyTextBCrypt(
		text: String?,
		hash: String?
	): AppResult<Unit> {
		if (text != null && hash != null) {
			return try {
				val result = BCrypt.verifyer().verify(text.toCharArray(), hash)
				if (result.verified) {
					AppResult.Success(Unit)
				}
				else {
					AppResult.Failure(HttpStatusCode.NotFound, "Password verification failed.")
				}
			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.NotFound, "Failed to verify text: ${e.message}.")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "Text and hash must not be null.")
		}
	}

	override suspend fun hashTextSHA512(text: String?): AppResult<String> {
		if (text != null) {
			return try {
				val digest = MessageDigest.getInstance("SHA-512")
				val hashBytes = digest.digest(text.toByteArray())
				val hashString = hashBytes.joinToString("") { "%02x".format(it) }
				AppResult.Success(hashString)
			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.NotFound, "Failed to hash text: ${e.message}.")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.NotFound, "Text must not be null.")
		}
	}

	// ========== EMAIL VERIFICATION METHODS ==========
	override suspend fun sendVerificationEmail(userId: UUID, email: String): AppResult<Unit> {
		return try {
			val tokenResult = generateEmailVerificationToken(userId, email)
			if (tokenResult is AppResult.Success) {
				val token = tokenResult.data
				val verificationLink = buildVerificationLink(token)

				sendEmail(
					to = email,
					subject = "Verify your email address",
					body = buildVerificationEmailBody(verificationLink)
				)

				AppResult.Success(Unit)
			}
			else {
				AppResult.Failure(HttpStatusCode.NotFound, "Email verification failed.")
			}
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.NotFound, "Failed to send verification email: ${e.message}.")
		}
	}

	override suspend fun verifyEmail(token: String): AppResult<Unit> {
		return withTransaction {
			try {
				// Verify the token
				val now = Instant.now()
				val decodedToken = verifyEmailVerificationToken(token)
				if (decodedToken != null) {
					val userId = UUID.fromString(decodedToken.subject)
					val email = decodedToken.getClaim("email").asString()
					val expiresAt = decodedToken.getClaim("exp").asInstant()
					if (expiresAt != null && expiresAt >= now) {
						if (userId != null && email != null) {
							// Get the user
							val userResult = userService.getById(userId)
							if (userResult is AppResult.Success) {
								val user = userResult.data
								// Check if email matches
								if (user.email == email) {
									// Check if already verified
									if (!user.isEmailVerified) {
										// Update user to mark email as verified
										val updatedUser = User(
											id = user.id,
											email = user.email,
											username = user.username,
											isEmailVerified = true,
											createdAt = user.createdAt,
											updatedAt = Instant.now()
										)
										val updateResult = userService.update(updatedUser)
										if (updateResult is AppResult.Success) {
											return@withTransaction AppResult.Success(Unit)
										}
										else {
											return@withTransaction AppResult.Failure(
												HttpStatusCode.NotFound,
												"Failed to update user verification status."
											)

										}
									}
									else {
										return@withTransaction AppResult.Success(Unit)
									}

								}
								else {
									return@withTransaction AppResult.Failure(
										HttpStatusCode.NotFound,
										"Email does not match."
									)
								}

							}
							else {
								return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
							}
						}
						else {
							return@withTransaction AppResult.Failure(
								HttpStatusCode.NotFound,
								"Invalid verification token."
							)
						}
					}
					else {
						return@withTransaction AppResult.Failure(
							HttpStatusCode.Unauthorized,
							"Email verification expired."
						)
					}

				}
				else {
					return@withTransaction AppResult.Failure(
						HttpStatusCode.NotFound,
						"Invalid or expired verification token."
					)
				}

			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.NotFound, "Email verification failed: ${e.message}.")
			}
		}
	}

	override suspend fun resendVerificationEmail(email: String): AppResult<Unit> {
		return withTransaction {
			// Find user by email
			val userResult = userService.getByEmail(email)
			if (userResult is AppResult.Success) {
				val user = userResult.data
				// Check if already verified
				if (!user.isEmailVerified) {
					// Send verification email
					return@withTransaction sendVerificationEmail(user.id, user.email)
				}
				else {
					return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Email is already verified.")
				}
			}
			else {
				return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "User not found.")
			}
		}
	}

	override suspend fun generateEmailVerificationToken(userId: UUID, email: String): AppResult<String> {
		return try {
			val now = Instant.now()
			val expiresAt = now.plus(Duration.ofHours(24)) // 24 hours for email verification
			val token = JWT.create()
				.withIssuer(jwtConfig.iss)
				.withAudience(jwtConfig.aud)
				.withSubject(userId.toString())
				.withIssuedAt(now)
				.withExpiresAt(expiresAt)
				.withJWTId(UUID.randomUUID().toString())
				.withClaim("typ", TokenType.EMAIL_VERIFICATION.name)
				.withClaim("email", email)
				.sign(jwtConfig.algAccess)

			AppResult.Success(token)
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.NotFound, "Failed to generate email verification token: ${e.message}.")
		}
	}

	// ========== PRIVATE HELPER METHODS ==========
	private fun verifyEmailVerificationToken(token: String): DecodedJWT? {
		return try {
			JWT.require(jwtConfig.algAccess)
				.withIssuer(jwtConfig.iss)
				.withAudience(jwtConfig.aud)
				.withClaim("typ", TokenType.EMAIL_VERIFICATION.name)
				.build()
				.verify(token)
		}
		catch (e: JWTVerificationException) {
			null
		}
	}

	private fun buildVerificationLink(token: String): String {
		// Use the base URL from email config or default to localhost
		return "${emailConfig.verificationBaseUrl}/auth/verify-email?token=$token"
	}

	private fun buildVerificationEmailBody(verificationLink: String): String {
		return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Verify Your Email - Calmed</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { padding: 30px; background-color: #f9f9f9; border-radius: 0 0 5px 5px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { margin-top: 30px; font-size: 12px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Welcome to Calmed!</h1>
                </div>
                <div class="content">
                    <h2>Verify Your Email Address</h2>
                    <p>Thank you for registering with Calmed. To complete your registration and start using our platform, please verify your email address by clicking the button below:</p>
                    
                    <div style="text-align: center;">
                        <a href="$verificationLink" class="button">Verify Email Address</a>
                    </div>
                    
                    <p>If the button doesn't work, you can also copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; background-color: #eee; padding: 10px; border-radius: 3px;">
                        $verificationLink
                    </p>
                    
                    <p>This verification link will expire in <strong>24 hours</strong>.</p>
                    
                    <p>If you didn't create an account with Calmed, you can safely ignore this email.</p>
                    
                    <div class="footer">
                        <p>Best regards,<br>The Calmed Team</p>
                        <p>© ${
			Instant.now().atZone(java.time.ZoneId.systemDefault()).year
		} Calmed. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
	}

	private fun sendEmail(to: String, subject: String, body: String) {
		val email = HtmlEmail()

		email.hostName = emailConfig.host
		email.setSmtpPort(emailConfig.port)

		email.setAuthenticator(
			DefaultAuthenticator(
				emailConfig.username,
				emailConfig.password
			)
		)
		// ✅ Gmail 587 settings
		email.isSSLOnConnect = false
		email.isStartTLSEnabled = true
		email.isStartTLSRequired = true

		email.setFrom(emailConfig.fromEmail, emailConfig.fromName)
		email.subject = subject
		email.setHtmlMsg(body)
		email.setTextMsg("Please verify your email by opening this link.")
		email.addTo(to)

		email.send()
	}
}