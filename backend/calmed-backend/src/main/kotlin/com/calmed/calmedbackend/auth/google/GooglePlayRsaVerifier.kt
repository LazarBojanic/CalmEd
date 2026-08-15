package com.calmed.calmedbackend.auth.google

import org.slf4j.LoggerFactory
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object GooglePlayRsaVerifier {
    private val logger = LoggerFactory.getLogger(GooglePlayRsaVerifier::class.java)
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA256withRSA"

    fun generatePublicKey(encodedPublicKey: String): PublicKey {
        val cleanKey = encodedPublicKey.replace("\\s+".toRegex(), "")
        val decodedKey = Base64.getDecoder().decode(cleanKey)
        val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
    }

    fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        if (signedData.isBlank() || signature.isBlank()) {
            return false
        }
        return try {
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray(Charsets.UTF_8))
            val signatureBytes = Base64.getDecoder().decode(signature.trim())
            sig.verify(signatureBytes)
        } catch (e: Exception) {
            logger.warn("Signature verification failed with exception: ${e.message}")
            false
        }
    }

    fun verify(encodedPublicKey: String, signedData: String, signature: String): Boolean {
        if (encodedPublicKey.isBlank() || signedData.isBlank() || signature.isBlank()) {
            return false
        }
        return try {
            val publicKey = generatePublicKey(encodedPublicKey)
            verify(publicKey, signedData, signature)
        } catch (e: Exception) {
            logger.error("Failed to generate public key or verify signature: ${e.message}", e)
            false
        }
    }
}
