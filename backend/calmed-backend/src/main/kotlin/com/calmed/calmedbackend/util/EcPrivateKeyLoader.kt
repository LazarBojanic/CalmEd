package com.calmed.calmedbackend.util

import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

object EcPrivateKeyLoader {
    fun load(pem: String): ECPrivateKey {
        val clean = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\n", "\n")
            .replace(Regex("\\s"), "")
        val keyBytes = Base64.getDecoder().decode(clean)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("EC").generatePrivate(keySpec) as ECPrivateKey
    }
}
