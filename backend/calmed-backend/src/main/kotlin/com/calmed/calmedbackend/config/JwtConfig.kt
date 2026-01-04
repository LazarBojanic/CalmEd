package com.calmed.calmedbackend.config

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.algorithms.Algorithm.HMAC256
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
		fun from(config: ApplicationConfig): JwtConfig {
			val accessSecret = config.property("jwt.access_secret").getString()
			val refreshSecret = config.property("jwt.refresh_secret").getString()
			val emailVerificationSecret = config.property("jwt.email_verification_secret").getString()
			val passwordResetSecret = config.property("jwt.password_reset_secret").getString()

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

		private fun parseAlg(alg: String, secret: String): Algorithm {
			return when (alg) {
				"HS512" -> HMAC512(secret)
				else -> HMAC256(secret)
			}
		}
	}
}
