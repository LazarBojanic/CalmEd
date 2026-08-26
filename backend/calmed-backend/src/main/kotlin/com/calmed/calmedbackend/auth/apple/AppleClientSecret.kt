package com.calmed.calmedbackend.auth.apple

import com.calmed.calmedbackend.config.AppleConfig
import com.calmed.calmedbackend.util.EcPrivateKeyLoader
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.*

object AppleClientSecret {

    fun generate(appleConfig: AppleConfig): String {
        val now = Instant.now()
        val exp = now.plusSeconds(5 * 60)

        val claims = JWTClaimsSet.Builder()
            .issuer(appleConfig.teamId)
            .subject(appleConfig.clientId)
            .audience("https://appleid.apple.com")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(exp))
            .build()

        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .keyID(appleConfig.keyId)
            .build()

        val jwt = SignedJWT(header, claims)
        val signer = ECDSASigner(EcPrivateKeyLoader.load(appleConfig.privateKeyPem))
        jwt.sign(signer)
        return jwt.serialize()
    }
}
