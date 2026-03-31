package com.calmed.calmedbackend.service.implementation

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import com.calmed.calmedbackend.auth.TokenType
import com.calmed.calmedbackend.auth.apple.AppleClaims
import com.calmed.calmedbackend.auth.apple.AppleIdTokenVerifier
import com.calmed.calmedbackend.config.AppleConfig
import com.calmed.calmedbackend.config.EmailConfig
import com.calmed.calmedbackend.config.GoogleOAuthConfig
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
import com.calmed.calmedbackend.model.raw.userinfo.tics.UserInfoTics
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.service.specification.IAuthCredentialService
import com.calmed.calmedbackend.service.specification.IAuthService
import com.calmed.calmedbackend.service.specification.IRefreshTokenService
import com.calmed.calmedbackend.service.specification.IUserInfoTicsService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

class AuthService(private val userService: IUserService,
				  private val authCredentialService: IAuthCredentialService,
				  private val refreshTokenService: IRefreshTokenService,
				  private val userInfoTicsService: IUserInfoTicsService,
				  private val jwtConfig: JwtConfig,
				  private val emailConfig: EmailConfig,
				  private val appleConfig: AppleConfig,
				  private val googleOAuthConfig: GoogleOAuthConfig
) : IAuthService {
	private val googleHttp = HttpClient {
		install(ContentNegotiation) {
			json(Json { ignoreUnknownKeys = true })
		}
	}
	private val appleHttp = HttpClient {
		install(ContentNegotiation) {
			json(Json { ignoreUnknownKeys = true })
		}
	}
	@Serializable
	data class GoogleTokenInfo(val sub: String? = null,
							   val email: String? = null,
							   val email_verified: String? = null,
							   val aud: String? = null
	)

	private suspend fun verifyAppleIdentityToken(identityToken: String): AppResult<AppleClaims> {
		return try {
			val verifier = AppleIdTokenVerifier(appleHttp, appleConfig)
			val claims = verifier.verify(identityToken)

			if (claims.sub.isEmpty()) {
				AppResult.Failure(HttpStatusCode.Unauthorized, "Apple token missing sub")
			} else {
				AppResult.Success(claims)
			}
		} catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.Unauthorized, "Invalid Apple token: ${e.message}")
		}
	}

	private suspend fun verifyGoogleIdToken(idToken: String): AppResult<GoogleTokenInfo> {
		return try {
			val info: GoogleTokenInfo = googleHttp.get("https://oauth2.googleapis.com/tokeninfo") {
				url { parameters.append("id_token", idToken) }
			}.body()

			if (info.email.isNullOrBlank()) {
				return AppResult.Failure(HttpStatusCode.Unauthorized, "Google token missing email")
			}

			if (info.email_verified != "true") {
				return AppResult.Failure(HttpStatusCode.Unauthorized, "Google email not verified")
			}

			AppResult.Success(info)
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.Unauthorized, "Invalid Google token: ${e.message}")
		}
	}
	private fun base64UrlDecode(s: String): ByteArray {
		val padded = when (s.length % 4) {
			2 -> "$s=="
			3 -> "$s="
			else -> s
		}
		return java.util.Base64.getUrlDecoder().decode(padded)
	}

	override fun accessVerifier(): JWTVerifier {
		return JWT.require(jwtConfig.accessAlg).withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).build()
	}

	override fun refreshVerifier(): JWTVerifier {
		return JWT.require(jwtConfig.refreshAlg).withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).build()
	}

	override fun emailVerificationVerifier(): JWTVerifier {
		return JWT.require(jwtConfig.emailVerificationAlg).withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).build()
	}

	override fun passwordResetVerifier(): JWTVerifier {
		return JWT.require(jwtConfig.passwordResetAlg).withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).build()
	}

	override suspend fun generatePasswordResetToken(userId: UUID, email: String
	): AppResult<String> {
		return try {
			val now = Instant.now()
			val token =
				JWT.create().withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).withSubject(userId.toString())
					.withIssuedAt(now).withExpiresAt(now.plus(jwtConfig.passwordResetTtl))
					.withJWTId(UUID.randomUUID().toString()).withClaim("typ", TokenType.PASSWORD_RESET.name)
					.withClaim("email", email).sign(jwtConfig.passwordResetAlg)

			AppResult.Success(token)
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.NotFound, "Failed to generate email verification token: ${e.message}.")
		}
	}

	override suspend fun register(dto: RegisterDto): AppResult<TokenPairDto> {
		return withTransaction {
			val passwordValidationResult = validatePassword(dto.password, dto.confirmPassword)
			when (passwordValidationResult) {
				is AppResult.Success -> {
					val existingUserResult = userService.getByEmail(dto.email)
					when (existingUserResult) {
						is AppResult.Failure -> {
							val newUser = User.createNew(
								email = dto.email, username = dto.username, isEmailVerified = false, isOnboarded = false
							)
							val createdUserResult = userService.create(newUser)
							when (createdUserResult) {
								is AppResult.Success -> {
									val passwordHashResult = hashTextBCrypt(dto.password)
									when (passwordHashResult) {
										is AppResult.Success -> {
											val authCredential = AuthCredential.createNew(
												userId = createdUserResult.data.id,
												type = AuthCredentialType.BASIC,
												passwordHash = passwordHashResult.data
											)
											val createdAuthCredentialResult =
												authCredentialService.create(authCredential)
											when (createdAuthCredentialResult) {
												is AppResult.Success -> {
													CoroutineScope(Dispatchers.IO).launch {
														val emailSentResult = sendVerificationEmail(
															createdUserResult.data.id, createdUserResult.data.email
														)
														when (emailSentResult) {
															is AppResult.Success -> {
																println("Email sent to ${createdUserResult.data.email}")
															}

															is AppResult.Failure -> {
																println(emailSentResult.message)
															}
														}
													}
													val newUserInfoTics = UserInfoTics.createNew(
														userId = newUser.id,
														preferredName = null,
														age = null,
														stressLevel = null,
														tickType = null,
														tickFrequency = null,
														goal = null,
														followProgress = null
													)
													val userInfoTicsResult =
														userInfoTicsService.create(newUserInfoTics)
													when (userInfoTicsResult) {
														is AppResult.Success -> {
															return@withTransaction createTokenPair(
																createdUserResult.data.id, createdUserResult.data.email
															)
														}

														is AppResult.Failure -> {
															return@withTransaction AppResult.Failure(
																userInfoTicsResult.httpStatusCode,
																"Failed to create user info tics. ${userInfoTicsResult.message}"
															)
														}
													}
												}

												is AppResult.Failure -> {
													return@withTransaction AppResult.Failure(
														createdAuthCredentialResult.httpStatusCode,
														"Failed to create authentication credentials. ${createdAuthCredentialResult.message}"
													)
												}
											}

										}

										is AppResult.Failure -> {
											return@withTransaction AppResult.Failure(
												passwordHashResult.httpStatusCode,
												"Failed to hash password. ${passwordHashResult.message}"
											)
										}
									}
								}

								is AppResult.Failure -> {
									return@withTransaction AppResult.Failure(
										createdUserResult.httpStatusCode,
										"Failed to create user. ${createdUserResult.message}"
									)
								}
							}
						}

						is AppResult.Success -> {
							return@withTransaction AppResult.Failure(
								HttpStatusCode.Unauthorized, "Email already exists. "
							)
						}
					}
				}

				is AppResult.Failure -> {
					return@withTransaction AppResult.Failure(
						passwordValidationResult.httpStatusCode, "Invalid password. ${passwordValidationResult.message}"
					)
				}
			}
		}
	}

	override suspend fun login(dto: LoginDto): AppResult<TokenPairDto> {
		return withTransaction {
			val userResult = userService.getByEmail(dto.email)
			when (userResult) {
				is AppResult.Success -> {
					if (userResult.data.isEmailVerified) {
						val authCredentialResult = authCredentialService.getByUserIdAndType(
							userResult.data.id, AuthCredentialType.BASIC
						)
						when (authCredentialResult) {
							is AppResult.Success -> {
								val verifyResult = verifyTextBCrypt(
									dto.password, authCredentialResult.data.passwordHash
								)
								when (verifyResult) {
									is AppResult.Success -> {
										return@withTransaction createTokenPair(
											userResult.data.id, userResult.data.email
										)

									}

									is AppResult.Failure -> {
										return@withTransaction AppResult.Failure(
											verifyResult.httpStatusCode, "Invalid password. ${verifyResult.message}"
										)

									}
								}
							}

							is AppResult.Failure -> {
								return@withTransaction AppResult.Failure(
									authCredentialResult.httpStatusCode,
									"No authentication credentials found. ${authCredentialResult.message}"
								)
							}
						}
					}
					else {
						return@withTransaction AppResult.Failure(HttpStatusCode.Unauthorized, "Email not verified.")
					}
				}

				is AppResult.Failure -> {
					return@withTransaction AppResult.Failure(
						userResult.httpStatusCode, "Invalid email. ${userResult.message}"
					)

				}
			}
		}
	}

	override suspend fun loginWithGoogle(idToken: String): AppResult<TokenPairDto> {
		return withTransaction {
			val tokenInfoResult = verifyGoogleIdToken(idToken)
			val result: AppResult<TokenPairDto> = when (tokenInfoResult) {
				is AppResult.Success -> {
					val tokenInfo = tokenInfoResult.data
					val email = tokenInfo.email
					val googleSub = tokenInfo.sub

					if (email.isNullOrBlank()) {
						AppResult.Failure(HttpStatusCode.Unauthorized, "Google token missing email")
					}
					else if (googleSub.isNullOrBlank()) {
						AppResult.Failure(HttpStatusCode.Unauthorized, "Google token missing sub")
					}
					else {
						val existingGoogle = authCredentialService.findRawByProviderUserIdAndType(
							providerUserId = googleSub, type = AuthCredentialType.GOOGLE
						)

						if (existingGoogle != null) {
							createTokenPair(existingGoogle.userId, email)
						}
						else {
							val username = email.substringBefore("@")
							val userResult = userService.getByEmail(email)
							var createdNewUser = false
							val resolvedUserResult = when (userResult) {
								is AppResult.Success -> userResult
								is AppResult.Failure -> {
									val newUser = User.createNew(
										email = email, username = username, isEmailVerified = true, isOnboarded = false
									)
									val createdUserResult = userService.create(newUser)
									when (createdUserResult) {
										is AppResult.Success -> {
											createdNewUser = true
											createdUserResult
										}

										is AppResult.Failure -> AppResult.Failure(
											createdUserResult.httpStatusCode,
											"Failed to create google user. ${createdUserResult.message}"
										)
									}
								}
							}

							when (resolvedUserResult) {
								is AppResult.Success -> {
									val user = resolvedUserResult.data

									if (!user.isEmailVerified) {
										AppResult.Failure(HttpStatusCode.Unauthorized, "Email not verified.")
									}
									else {
										val googleCred = AuthCredential.createNew(
											userId = user.id,
											type = AuthCredentialType.GOOGLE,
											passwordHash = null,
											providerUserId = googleSub
										)
										val createdGoogleCredResult = authCredentialService.create(googleCred)

										when (createdGoogleCredResult) {
											is AppResult.Success -> {
												if (createdNewUser) {
													val newUserInfoTics = UserInfoTics.createNew(
														userId = user.id,
														preferredName = null,
														age = null,
														stressLevel = null,
														tickType = null,
														tickFrequency = null,
														goal = null,
														followProgress = null
													)
													val userInfoTicsResult =
														userInfoTicsService.create(newUserInfoTics)

													when (userInfoTicsResult) {
														is AppResult.Success -> createTokenPair(user.id, email)
														is AppResult.Failure -> AppResult.Failure(
															userInfoTicsResult.httpStatusCode,
															"Failed to create user info tics. ${userInfoTicsResult.message}"
														)
													}
												}
												else {
													createTokenPair(user.id, email)
												}
											}

											is AppResult.Failure -> AppResult.Failure(
												createdGoogleCredResult.httpStatusCode,
												"Failed to create authentication credentials. ${createdGoogleCredResult.message}"
											)
										}
									}
								}

								is AppResult.Failure -> AppResult.Failure(
									resolvedUserResult.httpStatusCode, resolvedUserResult.message
								)
							}
						}
					}
				}

				is AppResult.Failure -> AppResult.Failure(
					tokenInfoResult.httpStatusCode, tokenInfoResult.message
				)
			}

			result
		}
	}

	override suspend fun loginWithApple(identityToken: String): AppResult<TokenPairDto> {
		return withTransaction {
			val tokenInfoResult = verifyAppleIdentityToken(identityToken)

			when (tokenInfoResult) {
				is AppResult.Success -> {
					val tokenInfo = tokenInfoResult.data
					val appleSub = tokenInfo.sub
					val email = tokenInfo.email
					val safeEmail = email ?: "apple_${appleSub}@apple.local"

					val existingApple = authCredentialService.findRawByProviderUserIdAndType(
						providerUserId = appleSub,
						type = AuthCredentialType.APPLE
					)

					if (existingApple != null) {
						// email može biti null -> koristi postojeći user email iz baze
						val userRes = userService.getById(existingApple.userId)
						return@withTransaction when (userRes) {
							is AppResult.Success -> {
								val emailToUse = userRes.data.email.ifBlank { safeEmail }
								createTokenPair(existingApple.userId, emailToUse)
							}
							is AppResult.Failure -> AppResult.Failure(userRes.httpStatusCode, userRes.message)
						}
					}



					val username = safeEmail.substringBefore("@")
					val userResult = userService.getByEmail(safeEmail)
					var createdNewUser = false

					val resolvedUserResult = when (userResult) {
						is AppResult.Success -> userResult
						is AppResult.Failure -> {
							val newUser = User.createNew(
								email = safeEmail,
								username = username,
								isEmailVerified = true,
								isOnboarded = false
							)
							val createdUserResult = userService.create(newUser)
							when (createdUserResult) {
								is AppResult.Success -> {
									createdNewUser = true
									createdUserResult
								}
								is AppResult.Failure -> AppResult.Failure(
									createdUserResult.httpStatusCode,
									"Failed to create apple user. ${createdUserResult.message}"
								)
							}
						}
					}

					when (resolvedUserResult) {
						is AppResult.Success -> {
							val user = resolvedUserResult.data

							if (!user.isEmailVerified) {
								AppResult.Failure(HttpStatusCode.Unauthorized, "Email not verified.")
							} else {
								val appleCred = AuthCredential.createNew(
									userId = user.id,
									type = AuthCredentialType.APPLE,
									passwordHash = null,
									providerUserId = appleSub
								)

								val createdAppleCredResult = authCredentialService.create(appleCred)

								when (createdAppleCredResult) {
									is AppResult.Success -> {
										if (createdNewUser) {
											val newUserInfoTics = UserInfoTics.createNew(
												userId = user.id,
												preferredName = null,
												age = null,
												stressLevel = null,
												tickType = null,
												tickFrequency = null,
												goal = null,
												followProgress = null
											)
											val userInfoTicsResult =
												userInfoTicsService.create(newUserInfoTics)

											when (userInfoTicsResult) {
												is AppResult.Success -> createTokenPair(user.id, safeEmail)
												is AppResult.Failure -> AppResult.Failure(
													userInfoTicsResult.httpStatusCode,
													"Failed to create user info tics. ${userInfoTicsResult.message}"
												)
											}
										} else {
											createTokenPair(user.id, safeEmail)
										}
									}

									is AppResult.Failure -> AppResult.Failure(
										createdAppleCredResult.httpStatusCode,
										"Failed to create authentication credentials. ${createdAppleCredResult.message}"
									)
								}
							}
						}

						is AppResult.Failure -> AppResult.Failure(
							resolvedUserResult.httpStatusCode,
							resolvedUserResult.message
						)
					}
				}

				is AppResult.Failure -> AppResult.Failure(
					tokenInfoResult.httpStatusCode,
					tokenInfoResult.message
				)
			}
		}
	}


	override suspend fun logout(userId: UUID): AppResult<Unit> {
		return withTransaction {
			refreshTokenService.revokeAllByUserId(userId, null)
		}
	}

	override suspend fun createTokenPair(userId: UUID, email: String): AppResult<TokenPairDto> {
		return withTransaction {
			val now = Instant.now()
			val accessTokenResult = generateAccessToken(userId, email, now)
			when (accessTokenResult) {
				is AppResult.Success -> {
					val refreshTokenResult = generateRefreshToken(userId, email, now)
					when (refreshTokenResult) {
						is AppResult.Success -> {
							val refreshTokenJwt = refreshTokenResult.data
							val tokenHashResult = hashTextSHA512(refreshTokenJwt)
							when (tokenHashResult) {
								is AppResult.Success -> {
									val decodedRefreshToken = try {
										refreshVerifier().verify(refreshTokenJwt)
									}
									catch (e: JWTVerificationException) {
										return@withTransaction AppResult.Failure(
											HttpStatusCode.Unauthorized, "Failed to verify refresh token."
										)
									}
									val refreshTokenId = UUID.fromString(decodedRefreshToken.id)
									val refreshToken = RefreshToken.createNew(
										id = refreshTokenId,
										replacedBy = null,
										userId = userId,
										tokenHash = tokenHashResult.data,
										issuedAt = now,
										expiresAt = now.plus(jwtConfig.refreshTtl),
										revokedAt = null
									)
									val storedRefreshTokenResult = refreshTokenService.create(refreshToken)
									when (storedRefreshTokenResult) {
										is AppResult.Success -> {
											return@withTransaction AppResult.Success(
												TokenPairDto(
													access = accessTokenResult.data, refresh = refreshTokenJwt
												)
											)
										}

										is AppResult.Failure -> {
											return@withTransaction AppResult.Failure(
												storedRefreshTokenResult.httpStatusCode,
												"Failed to store refresh token. ${storedRefreshTokenResult.message}"
											)
										}
									}

								}

								is AppResult.Failure -> {
									return@withTransaction AppResult.Failure(
										tokenHashResult.httpStatusCode,
										"Failed to hash refresh token. ${tokenHashResult.message}"
									)
								}
							}

						}

						is AppResult.Failure -> {
							return@withTransaction AppResult.Failure(
								refreshTokenResult.httpStatusCode,
								"Failed to generate refresh token. ${refreshTokenResult.message}"
							)
						}
					}

				}

				is AppResult.Failure -> {
					return@withTransaction AppResult.Failure(
						accessTokenResult.httpStatusCode,
						"Failed to generate access token. ${accessTokenResult.message}"
					)

				}
			}
		}
	}

	override suspend fun generateAccessToken(id: UUID, email: String, now: Instant
	): AppResult<String> {
		return try {
			println("JWT GENERATE → iss='${jwtConfig.iss}', aud='${jwtConfig.aud}'")
			val token = JWT.create().withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).withSubject(id.toString())
				.withIssuedAt(now).withExpiresAt(now.plus(jwtConfig.accessTtl)).withJWTId(UUID.randomUUID().toString())
				.withClaim("typ", TokenType.ACCESS.name).withClaim("email", email).sign(jwtConfig.accessAlg)

			AppResult.Success(token)
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to generate access token: ${e.message}.")
		}
	}

	override suspend fun generateRefreshToken(userId: UUID, email: String, now: Instant
	): AppResult<String> {
		return try {
			val refreshTokenId = UUID.randomUUID()
			val expiresAt = now.plus(jwtConfig.refreshTtl)
			println("JWT REFRESH → iss='${jwtConfig.iss}', aud='${jwtConfig.aud}'")
			val token =
				JWT.create().withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).withSubject(userId.toString())
					.withIssuedAt(now).withExpiresAt(expiresAt).withJWTId(refreshTokenId.toString())
					.withClaim("typ", TokenType.REFRESH.name).withClaim("email", email).sign(jwtConfig.refreshAlg)

			AppResult.Success(token)
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to generate refresh token: ${e.message}.")
		}
	}

	override suspend fun refresh(dto: RefreshDto): AppResult<TokenPairDto> {
		return withTransaction {
			val decodedRefreshToken = try {
				refreshVerifier().verify(dto.refresh)
			}
			catch (e: JWTVerificationException) {
				return@withTransaction AppResult.Failure(HttpStatusCode.Unauthorized, "Invalid refresh token.")
			}
			val tokenType = decodedRefreshToken.getClaim("typ").asString()
			if (tokenType == TokenType.REFRESH.name) {
				val tokenId = decodedRefreshToken.id
				val userId = decodedRefreshToken.subject
				val email = decodedRefreshToken.getClaim("email").asString()

				if (tokenId != null && userId != null && email != null) {
					val refreshTokenUuid = UUID.fromString(tokenId)
					val userUuid = UUID.fromString(userId)
					val storedTokenResult = refreshTokenService.getById(refreshTokenUuid)
					when (storedTokenResult) {
						is AppResult.Success -> {
							val storedToken = storedTokenResult.data.toRaw()

							if (storedToken.isActive()) {
								val incomingHashResult = hashTextSHA512(dto.refresh)
								when (incomingHashResult) {
									is AppResult.Success -> {
										if (incomingHashResult.data == storedToken.tokenHash) {
											val newTokenPairResult = createTokenPair(userUuid, email)
											when (newTokenPairResult) {
												is AppResult.Success -> {
													val newDecodedToken = try {
														refreshVerifier().verify(newTokenPairResult.data.refresh)
													}
													catch (e: JWTVerificationException) {
														return@withTransaction AppResult.Failure(
															HttpStatusCode.Unauthorized,
															"Failed to decode new refresh token"
														)
													}
													val newTokenId = UUID.fromString(newDecodedToken.id)
													val revokedToken = storedToken.copy(
														replacedBy = newTokenId,
														revokedAt = Instant.now(),
														updatedAt = Instant.now()
													)
													val updateResult = refreshTokenService.update(revokedToken)
													when (updateResult) {
														is AppResult.Success -> {
															return@withTransaction newTokenPairResult
														}

														is AppResult.Failure -> {
															return@withTransaction AppResult.Failure(
																HttpStatusCode.NotFound,
																"Failed to revoke old refresh token."
															)
														}
													}
												}

												is AppResult.Failure -> {
													return@withTransaction AppResult.Failure(
														newTokenPairResult.httpStatusCode,
														"Failed to create new tokens. ${newTokenPairResult.message}"
													)
												}
											}

										}
										else {
											return@withTransaction AppResult.Failure(
												HttpStatusCode.Unauthorized, "Token hash mismatch."
											)
										}
									}

									is AppResult.Failure -> {
										return@withTransaction AppResult.Failure(
											incomingHashResult.httpStatusCode,
											"Failed to hash incoming token. ${incomingHashResult.message}"
										)
									}
								}

							}
							else {
								logout(userUuid)
								return@withTransaction AppResult.Failure(
									HttpStatusCode.Unauthorized, "Refresh token is no longer active."
								)
							}
						}

						is AppResult.Failure -> {
							return@withTransaction AppResult.Failure(
								storedTokenResult.httpStatusCode,
								"Refresh token not found. ${storedTokenResult.message}"
							)

						}
					}

				}
				else {
					return@withTransaction AppResult.Failure(HttpStatusCode.Unauthorized, "Invalid token claims.")
				}
			}
			else {
				return@withTransaction AppResult.Failure(HttpStatusCode.Unauthorized, "Invalid token type.")
			}
		}
	}

	override suspend fun validatePassword(p: String?, c: String?
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
								return AppResult.Failure(HttpStatusCode.Unauthorized, "Passwords do not match.")
							}
						}
						else {
							return AppResult.Failure(
								HttpStatusCode.Unauthorized, "Password must contain at least one digit."
							)
						}
					}
					else {
						return AppResult.Failure(
							HttpStatusCode.Unauthorized, "Password must contain at least one lowercase letter."
						)
					}
				}
				else {
					return AppResult.Failure(
						HttpStatusCode.Unauthorized, "Password must contain at least one uppercase letter."
					)
				}
			}
			else {
				return AppResult.Failure(HttpStatusCode.Unauthorized, "Password must be at least 8 characters long.")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.Unauthorized, "Password must not be null.")
		}
	}

	override suspend fun hashTextBCrypt(text: String?): AppResult<String> {
		if (text != null) {
			return try {
				val hash = BCrypt.withDefaults().hashToString(12, text.toCharArray())
				AppResult.Success(hash)
			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to hash text: ${e.message}.")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.InternalServerError, "Text must not be null.")
		}
	}

	override suspend fun verifyTextBCrypt(text: String?, hash: String?
	): AppResult<Unit> {
		if (text != null && hash != null) {
			return try {
				val result = BCrypt.verifyer().verify(text.toCharArray(), hash)
				if (result.verified) {
					AppResult.Success(Unit)
				}
				else {
					AppResult.Failure(HttpStatusCode.Unauthorized, "Password verification failed.")
				}
			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to verify text. ${e.message}")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.InternalServerError, "Text and hash must not be null.")
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
				AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to hash text. ${e.message}")
			}
		}
		else {
			return AppResult.Failure(HttpStatusCode.InternalServerError, "Text must not be null.")
		}
	}

	override suspend fun sendVerificationEmail(userId: UUID, email: String): AppResult<Unit> {
		return try {
			val tokenResult = generateEmailVerificationToken(userId, email)
			when (tokenResult) {
				is AppResult.Success -> {
					val token = tokenResult.data
					val verificationLink = buildVerificationLink(token)

					sendEmail(
						to = email,
						subject = "Verify your email address",
						body = buildVerificationEmailBody(verificationLink)
					)

					AppResult.Success(Unit)
				}

				is AppResult.Failure -> {
					AppResult.Failure(tokenResult.httpStatusCode, "Email verification failed. ${tokenResult.message}")

				}
			}
		}
		catch (e: Exception) {
			AppResult.Failure(HttpStatusCode.InternalServerError, "Failed to send verification email. ${e.message}")
		}
	}

	override suspend fun verifyEmail(token: String): AppResult<Unit> {
		return withTransaction {
			try {
				val now = Instant.now()
				val decodedToken = try {
					emailVerificationVerifier().verify(token)
				}
				catch (e: JWTVerificationException) {
					return@withTransaction AppResult.Failure(HttpStatusCode.Unauthorized, "Token verification failed.")
				}
				if (decodedToken != null) {
					val userId = UUID.fromString(decodedToken.subject)
					val email = decodedToken.getClaim("email").asString()
					val expiresAt = decodedToken.getClaim("exp").asInstant()
					if (expiresAt != null && expiresAt >= now) {
						if (userId != null && email != null) {
							val userResult = userService.getById(userId)
							when (userResult) {
								is AppResult.Success -> {
									val user = userResult.data

									if (user.email == email) {
										if (!user.isEmailVerified) {
											val updatedUser = User(
												id = user.id,
												email = user.email,
												username = user.username,
												isEmailVerified = true,
												isOnboarded = user.isOnboarded,
												isPaid = user.isPaid,
												paymentType = user.paymentType,
												stripeCustomerId = user.stripeCustomerId,
												createdAt = user.createdAt,
												updatedAt = Instant.now()
											)
											val updateResult = userService.update(updatedUser)
											when (updateResult) {
												is AppResult.Success -> {
													return@withTransaction AppResult.Success(Unit)
												}

												is AppResult.Failure -> {
													return@withTransaction AppResult.Failure(
														updateResult.httpStatusCode,
														"Failed to update user verification status. ${updateResult.message}"
													)
												}
											}

										}
										else {
											return@withTransaction AppResult.Failure(
												HttpStatusCode.Unauthorized,
												"Email already verified."
											)
										}

									}
									else {
										return@withTransaction AppResult.Failure(
											HttpStatusCode.Unauthorized, "Email does not match."
										)
									}
								}

								is AppResult.Failure -> {
									return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "User not found.")

								}
							}

						}
						else {
							return@withTransaction AppResult.Failure(
								HttpStatusCode.Unauthorized, "Invalid verification token."
							)
						}
					}
					else {
						return@withTransaction AppResult.Failure(
							HttpStatusCode.Unauthorized, "Email verification expired."
						)
					}

				}
				else {
					return@withTransaction AppResult.Failure(
						HttpStatusCode.NotFound, "Invalid or expired verification token."
					)
				}

			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.InternalServerError, "Email verification failed: ${e.message}.")
			}
		}
	}

	override suspend fun resendVerificationEmail(email: String): AppResult<Unit> {
		return withTransaction {
			val userResult = userService.getByEmail(email)
			when (userResult) {
				is AppResult.Success -> {
					val user = userResult.data

					if (!user.isEmailVerified) {
						return@withTransaction sendVerificationEmail(user.id, user.email)
					}
					else {
						return@withTransaction AppResult.Failure(
							HttpStatusCode.BadRequest, "Email is already verified."
						)
					}
				}

				is AppResult.Failure -> {
					return@withTransaction AppResult.Failure(
						userResult.httpStatusCode, "User not found. ${userResult.message}"
					)

				}
			}
		}
	}

	override suspend fun generateEmailVerificationToken(userId: UUID, email: String): AppResult<String> {
		return try {
			val now = Instant.now()
			val token =
				JWT.create().withIssuer(jwtConfig.iss).withAudience(jwtConfig.aud).withSubject(userId.toString())
					.withIssuedAt(now).withExpiresAt(now.plus(jwtConfig.emailVerificationTtl))
					.withJWTId(UUID.randomUUID().toString()).withClaim("typ", TokenType.EMAIL_VERIFICATION.name)
					.withClaim("email", email).sign(jwtConfig.emailVerificationAlg)

			AppResult.Success(token)
		}
		catch (e: Exception) {
			AppResult.Failure(
				HttpStatusCode.InternalServerError, "Failed to generate email verification token. ${e.message}"
			)
		}
	}

	private fun buildVerificationLink(token: String): String {
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
				emailConfig.username, emailConfig.password
			)
		)

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

	override suspend fun sendPasswordResetEmail(email: String): AppResult<Unit> {
		val userResult = userService.getByEmail(email)
		when (userResult) {
			is AppResult.Success -> {
				val user = userResult.data
				val tokenResult = generatePasswordResetToken(user.id, user.email)

				when (tokenResult) {
					is AppResult.Success -> {
						val resetLink =
							"${emailConfig.verificationBaseUrl}/auth/reset-password?token=${tokenResult.data}"
						sendEmail(
							to = user.email,
							subject = "Reset your password",
							body = "Click here to reset your password: $resetLink"
						)
						return AppResult.Success(Unit)
					}

					is AppResult.Failure -> {
						return AppResult.Failure(
							tokenResult.httpStatusCode, "Failed to send reset email. ${tokenResult.message}"
						)

					}
				}

			}

			is AppResult.Failure -> {
				return AppResult.Success(Unit)
			}
		}

	}

	override suspend fun resetPassword(token: String, newPassword: String
	): AppResult<Unit> {
		return withTransaction {
			try {
				val decodedToken = try {
					passwordResetVerifier().verify(token)
				}
				catch (e: JWTVerificationException) {
					return@withTransaction AppResult.Failure(
						HttpStatusCode.Unauthorized, "Failed to verify token. ${e.message}"
					)
				}

				if (decodedToken != null && decodedToken.getClaim("typ").asString() == TokenType.PASSWORD_RESET.name) {
					val userId = UUID.fromString(decodedToken.subject)
					val credentialResult = authCredentialService.getByUserIdAndType(userId, AuthCredentialType.BASIC)

					when (credentialResult) {
						is AppResult.Success -> {
							val hashedPasswordResult = hashTextBCrypt(newPassword)
							when (hashedPasswordResult) {
								is AppResult.Success -> {
									val credential = credentialResult.data
									authCredentialService.update(
										AuthCredential(
											id = credential.id,
											userId = userId,
											type = credential.type,
											passwordHash = hashedPasswordResult.data,
											createdAt = credential.createdAt,
											updatedAt = Instant.now()
										)
									)
									AppResult.Success(Unit)
								}

								is AppResult.Failure -> {
									AppResult.Failure(
										hashedPasswordResult.httpStatusCode,
										"Failed to hash password. ${hashedPasswordResult.message}"
									)

								}
							}

						}

						is AppResult.Failure -> {
							AppResult.Failure(
								credentialResult.httpStatusCode, "Credentials not found. ${credentialResult.message}"
							)

						}
					}
				}
				else {
					return@withTransaction AppResult.Failure(HttpStatusCode.Unauthorized, "Invalid token.")
				}

			}
			catch (e: Exception) {
				AppResult.Failure(HttpStatusCode.InternalServerError, "Reset failed. ${e.message}")
			}
		}
	}

}
