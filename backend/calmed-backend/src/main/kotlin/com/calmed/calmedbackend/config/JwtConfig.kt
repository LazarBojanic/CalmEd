package com.calmed.calmedbackend.config

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.auth0.jwt.algorithms.Algorithm.HMAC384
import com.auth0.jwt.algorithms.Algorithm.HMAC512
import io.ktor.server.config.*
import java.time.Duration

data class JwtConfig(
	val accessAlg: Algorithm,
	val refreshAlg: Algorithm,
	val emailVerificationAlg: Algorithm,
	val passwordResetAlg: Algorithm,
	val iss: String,
	val aud: String,
	val accessTtl: Duration,
	val refreshTtl: Duration,
	val emailVerificationTtl: Duration,
	val passwordResetTtl: Duration,
) {
	companion object {
		private const val MIN_SECRET_LENGTH = 32

		fun from(config: ApplicationConfig): JwtConfig {
			val accessSecret = requireSecret(config, "jwt.access_secret", "JWT_ACCESS_SECRET")
			val refreshSecret = requireSecret(config, "jwt.refresh_secret", "JWT_REFRESH_SECRET")
			val emailVerificationSecret =
				requireSecret(config, "jwt.email_verification_secret", "JWT_EMAIL_VERIFICATION_SECRET")
			val passwordResetSecret =
				requireSecret(config, "jwt.password_reset_secret", "JWT_PASSWORD_RESET_SECRET")

			val secrets = listOf(accessSecret, refreshSecret, emailVerificationSecret, passwordResetSecret)
			check(secrets.toSet().size == secrets.size) {
				"JWT secrets must all be distinct. Reusing one secret across token types allows token-type confusion."
			}

			return JwtConfig(
				accessAlg = parseAlg(config.property("jwt.access_alg").getString(), accessSecret),
				refreshAlg = parseAlg(config.property("jwt.refresh_alg").getString(), refreshSecret),
				emailVerificationAlg = parseAlg(config.property("jwt.email_verification_alg").getString(), emailVerificationSecret),
				passwordResetAlg = parseAlg(config.property("jwt.password_reset_alg").getString(), passwordResetSecret),
				iss = config.property("jwt.iss").getString(),
				aud = config.property("jwt.aud").getString(),
				accessTtl = Duration.parse(config.property("jwt.access_ttl").getString()),
				refreshTtl = Duration.parse(config.property("jwt.refresh_ttl").getString()),
				emailVerificationTtl = Duration.parse(config.property("jwt.email_verification_ttl").getString()),
				passwordResetTtl = Duration.parse(config.property("jwt.password_reset_ttl").getString())
			)
		}

		private fun requireSecret(config: ApplicationConfig, key: String, envName: String): String {
			val secret = config.propertyOrNull(key)?.getString()
			check(!secret.isNullOrBlank() && secret != "placeholder") {
				"Missing JWT secret '$key'. Set the $envName environment variable to a strong, unique secret."
			}
			check(secret.length >= MIN_SECRET_LENGTH) {
				"JWT secret '$key' must be at least $MIN_SECRET_LENGTH characters long."
			}
			return secret
		}

		private fun parseAlg(alg: String, secret: String): Algorithm {
			return when (alg.uppercase()) {
				"HS256" -> HMAC256(secret)
				"HS384" -> HMAC384(secret)
				"HS512" -> HMAC512(secret)
				else -> throw IllegalArgumentException(
					"Unsupported JWT algorithm '$alg'. Allowed values: HS256, HS384, HS512."
				)
			}
		}
	}
}
