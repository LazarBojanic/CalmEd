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
	val accessSecret: String,
	val refreshSecret: String,
	val iss: String,
	val aud: String,
	val accessTtl: Duration,
	val refreshTtl: Duration
) {
	companion object {
		fun from(config: ApplicationConfig): JwtConfig {
			val accessSecret = config.property("jwt.access_secret").getString()
			val refreshSecret = config.property("jwt.refresh_secret").getString()
			val algAccessString = config.property("jwt.alg_access").getString()
			val algRefreshString = config.property("jwt.alg_refresh").getString()
			val algAccess = parseAlg(algAccessString, accessSecret)
			val algRefresh = parseAlg(algRefreshString, refreshSecret)
			val iss = config.property("jwt.iss").getString()
			val aud = config.property("jwt.aud").getString()
			val accessTtlString = config.property("jwt.access_ttl").getString()
			val refreshTtlString = config.property("jwt.refresh_ttl").getString()
			val accessTtl = Duration.parse(accessTtlString)
			val refreshTtl = Duration.parse(refreshTtlString)
			return JwtConfig(
				algAccess = algAccess,
				algRefresh = algRefresh,
				accessSecret = accessSecret,
				refreshSecret = refreshSecret,
				iss = iss,
				aud = aud,
				accessTtl = accessTtl,
				refreshTtl = refreshTtl
			)
		}
		fun parseAlg(algorithm: String, secret: String): Algorithm {
			return when(algorithm) {
				"HS512" -> HMAC512(secret)
				else -> HMAC256(secret)
			}
		}
	}
}