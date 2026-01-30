package com.calmed.calmedbackend.auth.apple

import com.calmed.calmedbackend.config.AppleConfig
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.util.Date

data class AppleIdTokenClaims(
    val subject: String, // sub (Apple user id)
    val email: String? = null,
    val emailVerified: Boolean? = null
)

class AppleIdTokenVerifier(
    private val http: HttpClient,
    private val config: AppleConfig
) {
    suspend fun verify(idToken: String): AppleIdTokenClaims {
        val jwt = SignedJWT.parse(idToken)

        // 1) issuer + audience + exp
        val claims = jwt.jwtClaimsSet

        val iss = claims.issuer
        require(iss == "https://appleid.apple.com") { "Invalid issuer: $iss" }

        val audOk = claims.audience?.contains(config.clientId) == true
        require(audOk) { "Invalid audience" }

        val exp = claims.expirationTime
        require(exp != null && exp.after(Date())) { "Token expired" }

        // 2) signature verify using Apple's JWKs
        val kid = jwt.header.keyID ?: error("Missing kid in token header")

        val jwksJson: String = http.get("https://appleid.apple.com/auth/keys").body()
        val jwkSet = JWKSet.parse(jwksJson)

        val jwk = jwkSet.keys.firstOrNull { it.keyID == kid }
            ?: error("Apple JWK not found for kid=$kid")

        require(jwt.header.algorithm == JWSAlgorithm.RS256) { "Unexpected alg: ${jwt.header.algorithm}" }

        val rsaKey = jwk.toRSAKey()
        val verifier = RSASSAVerifier(rsaKey.toRSAPublicKey())
        require(jwt.verify(verifier)) { "Invalid Apple token signature" }

        val sub = claims.subject ?: error("Missing sub")
        val email = claims.getStringClaim("email")
        val emailVerified = claims.getBooleanClaim("email_verified")

        return AppleIdTokenClaims(
            subject = sub,
            email = email,
            emailVerified = emailVerified
        )
    }
}
