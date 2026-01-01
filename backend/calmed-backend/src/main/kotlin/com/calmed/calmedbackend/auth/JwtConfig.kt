package com.calmed.calmedbackend.auth

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.auth0.jwt.algorithms.Algorithm.HMAC384
import com.auth0.jwt.algorithms.Algorithm.HMAC512
import io.ktor.server.config.*
import java.time.Duration


data class JwtConfig(
	val algAccess: Algorithm,
	val algRefresh: Algorithm,
	val iss: String,
	val aud: String,
	val accessTtl: Duration,
	val refreshTtl: Duration
) {
	companion object {
		fun from(config: ApplicationConfig): JwtConfig {
			val accessSecret = config.property("jwt.access_secret").getString()
			val refreshSecret = config.property("jwt.refresh_secret").getString()

			return JwtConfig(
				algAccess = parseAlg(config.property("jwt.alg_access").getString(), accessSecret),
				algRefresh = parseAlg(config.property("jwt.alg_refresh").getString(), refreshSecret),
				iss = config.property("jwt.iss").getString(),
				aud = config.property("jwt.aud").getString(),
				accessTtl = Duration.parse(config.property("jwt.access_ttl").getString()),
				refreshTtl = Duration.parse(config.property("jwt.refresh_ttl").getString())
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
