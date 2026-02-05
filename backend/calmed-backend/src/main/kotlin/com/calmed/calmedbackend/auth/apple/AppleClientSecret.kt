package com.calmed.calmedbackend.auth.apple

import com.calmed.calmedbackend.config.AppleConfig
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.koin.java.KoinJavaComponent.inject
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*

object AppleClientSecret {

    fun generate(appleConfig: AppleConfig): String {
        val now = Instant.now()
        val exp = now.plusSeconds(5 * 60)

        val claims = JWTClaimsSet.Builder()
            .issuer(appleConfig.teamId) // iss = Team ID
            .subject(appleConfig.clientId) // sub = client_id (Service ID)
            .audience("https://appleid.apple.com") // aud
            .issueTime(Date.from(now))
            .expirationTime(Date.from(exp))
            .build()

        val header = JWSHeader.Builder(JWSAlgorithm.RS512)
            .keyID(appleConfig.keyId) // kid = Key ID
            .build()

        val jwt = SignedJWT(header, claims)
        val signer = RSASSASigner(loadEcPrivateKey(appleConfig.privateKeyPem))
        jwt.sign(signer)
        return jwt.serialize()
    }

    private fun loadEcPrivateKey(pem: String): ECPrivateKey {
        // Apple .p8 je PKCS#8 EC private key
        val clean = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(clean)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("EC")
        return kf.generatePrivate(keySpec) as ECPrivateKey
    }
}
