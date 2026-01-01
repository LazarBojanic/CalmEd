package com.calmed.calmedbackend.auth

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.time.Duration


data class JwtConfig(
	val alg: Algorithm,
	val secret: String,
	val iss: String,
	val aud: String,
	val accessTtl: Duration,
	val refreshTtl: Duration
) {
	companion object {
		fun from(config: ApplicationConfig): JwtConfig {
			val secret = config.property("jwt.secret").getString()
			val algString = config.property("jwt.alg").getString()
			var alg: Algorithm
			if (algString == "HS256") {
				alg = Algorithm.HMAC256(secret)
			}
			else {
				throw IllegalArgumentException(
					"Unsupported algorithm: $algString"
				)
			}
			val iss = config.property("jwt.iss").getString()
			val aud = config.property("jwt.aud").getString()
			val accessTtlString = config.property("jwt.access_ttl").getString()
			val refreshTtlString = config.property("jwt.refresh_ttl").getString()
			val accessTtl = Duration.parse(accessTtlString)
			val refreshTtl = Duration.parse(refreshTtlString)
			return JwtConfig(alg, secret, iss, aud, accessTtl, refreshTtl)
		}
	}
}