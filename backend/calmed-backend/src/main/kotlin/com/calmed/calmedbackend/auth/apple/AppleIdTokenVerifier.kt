package com.calmed.calmedbackend.auth.apple

import com.calmed.calmedbackend.config.AppleConfig
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.util.Date



class AppleIdTokenVerifier(
    private val http: HttpClient,
    private val config: AppleConfig
) {
    suspend fun verify(idToken: String): AppleClaims {
        val jwt = SignedJWT.parse(idToken)

        // 1) signature verify using Apple's JWKs
        val claims = jwt.jwtClaimsSet
        val kid = jwt.header.keyID ?: error("Missing kid in token header")

        val jwksJson: String = http.get("https://appleid.apple.com/auth/keys").body()
        val jwkSet = JWKSet.parse(jwksJson)

        val jwk = jwkSet.keys.firstOrNull { it.keyID == kid }
            ?: error("Apple JWK not found for kid=$kid")

        val verifier = if (jwt.header.algorithm == JWSAlgorithm.ES256) {
            val ecKey = jwk.toECKey()
            ECDSAVerifier(ecKey.toECPublicKey())
        } else if (jwt.header.algorithm == JWSAlgorithm.RS256) {
            val rsaKey = jwk.toRSAKey()
            RSASSAVerifier(rsaKey.toRSAPublicKey())
        } else {
            error("Unexpected alg: ${jwt.header.algorithm}")
        }

        require(jwt.verify(verifier)) { "Invalid Apple token signature" }

        // 2) issuer + exp + audience
        require(claims.issuer == "https://appleid.apple.com") { "Invalid issuer: ${claims.issuer}" }
        val exp = claims.expirationTime
        require(exp != null && exp.after(Date())) { "Token expired" }

        val aud = claims.audience[0]
        require(aud.equals(config.clientId) || aud.equals(config.iosBundleId)) { "Invalid audience." }

        val iss = claims.issuer

        val sub = claims.subject ?: error("Missing sub")
        val email = claims.getStringClaim("email")
        val emailVerified = claims.getBooleanClaim("email_verified")
        return AppleClaims(
            iss = iss,
            aud = aud,
            sub = sub,
            email = email,
            emailVerified = emailVerified
        )
    }


}
