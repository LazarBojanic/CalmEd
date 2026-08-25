package com.calmed.calmedbackend.payment.apple

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.util.Base64 as JoseBase64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date

data class VerifiedAppleTransaction(
    val transactionId: String,
    val originalTransactionId: String,
    val bundleId: String,
    val productId: String,
    val type: String,
    val environment: String?,
    val revocationDate: Long?
)


class AppStoreServerApi(
    private val teamId: String,
    private val keyId: String,
    private val privateKeyPem: String,
    private val bundleId: String
) {
    private val logger = LoggerFactory.getLogger(AppStoreServerApi::class.java)

    private val httpClient = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    private val productionBase = "https://api.storekit.itunes.apple.com"
    private val sandboxBase = "https://api.storekit-sandbox.itunes.apple.com"

    fun isConfigured(): Boolean =
        teamId.isNotBlank() && teamId != "placeholder" &&
            keyId.isNotBlank() && keyId != "placeholder" &&
            privateKeyPem.isNotBlank() && privateKeyPem != "placeholder" &&
            bundleId.isNotBlank() && bundleId != "placeholder"


    suspend fun getVerifiedTransaction(transactionId: String): VerifiedAppleTransaction? {
        require(isConfigured()) { "Apple App Store Server API is not configured" }

        val jwt = productionGetTransaction(transactionId)
            ?: sandboxGetTransaction(transactionId)
            ?: return null

        return verifyJws(jwt)
    }

    private suspend fun productionGetTransaction(transactionId: String): String? =
        call(productionBase, transactionId)

    private suspend fun sandboxGetTransaction(transactionId: String): String? =
        call(sandboxBase, transactionId)

    private suspend fun call(baseUrl: String, transactionId: String): String? {
        val token = generateServerToken()
        return withContext(Dispatchers.IO) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/inApps/v1/transactions/$transactionId"))
                .header("Authorization", "Bearer $token")
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 404) {
                return@withContext null
            }
            if (response.statusCode() !in 200..299) {
                logger.warn("App Store Server API returned ${response.statusCode()}: ${response.body()}")
                return@withContext null
            }
            val obj = json.parseToJsonElement(response.body()).jsonObject
            obj["signedTransactionInfo"]?.jsonPrimitive?.content
        }
    }

    private fun verifyJws(jws: String): VerifiedAppleTransaction? {
        return try {
            val signedJWT = SignedJWT.parse(jws)
            val header = signedJWT.header
            if (header.algorithm != JWSAlgorithm.ES256) {
                logger.warn("Unexpected Apple JWS algorithm: ${header.algorithm}")
                return null
            }

            val certs = parseChain(header) ?: return null
            if (!verifyChain(certs)) return null
            val leaf = certs.first()

            val verifier = ECDSAVerifier(leaf.publicKey as ECPublicKey)
            if (!signedJWT.verify(verifier)) {
                logger.warn("Apple JWS signature verification failed")
                return null
            }

            val claims = signedJWT.jwtClaimsSet
            VerifiedAppleTransaction(
                transactionId = claims.getStringClaim("transactionId") ?: "",
                originalTransactionId = claims.getStringClaim("originalTransactionId") ?: "",
                bundleId = claims.getStringClaim("bundleId") ?: "",
                productId = claims.getStringClaim("productId") ?: "",
                type = claims.getStringClaim("type") ?: "",
                environment = claims.getStringClaim("environment"),
                revocationDate = (claims.getClaim("revocationDate") as? Number)?.toLong()
            )
        } catch (e: Exception) {
            logger.warn("Failed to verify Apple transaction JWS: ${e.message}")
            null
        }
    }

    private fun parseChain(header: JWSHeader): List<X509Certificate>? {
        val chain: List<JoseBase64> = header.x509CertChain ?: run {
            logger.warn("Apple JWS header has no x5c certificate chain")
            return null
        }
        if (chain.size < 2) {
            logger.warn("Apple JWS certificate chain too short")
            return null
        }
        return try {
            val cf = CertificateFactory.getInstance("X.509")
            chain.map { cf.generateCertificate(ByteArrayInputStream(it.decode())) as X509Certificate }
        } catch (e: Exception) {
            logger.warn("Failed to parse Apple JWS certificate chain: ${e.message}")
            null
        }
    }

    private fun verifyChain(certs: List<X509Certificate>): Boolean {
        return try {
            for (i in 0 until certs.size - 1) {
                certs[i].verify(certs[i + 1].publicKey)
            }

            val last = certs.last()
            val subject = last.subjectX500Principal.name
            val issuer = last.issuerX500Principal.name
            val isSelfSignedAppleRoot = subject == issuer && subject.contains("Apple Root CA")
            val isIssuedByAppleRoot = issuer.contains("Apple Root CA")
            if (!isSelfSignedAppleRoot && !isIssuedByAppleRoot) {
                logger.warn("Apple JWS chain does not anchor to an Apple Root CA: subject=$subject issuer=$issuer")
                return false
            }
            true
        } catch (e: Exception) {
            logger.warn("Apple JWS certificate chain verification failed: ${e.message}")
            false
        }
    }

    private fun generateServerToken(): String {
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .issuer(teamId)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(20 * 60)))
            .audience("appstoreconnect-v1")
            .claim("bid", bundleId)
            .build()

        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .keyID(keyId)
            .type(JOSEObjectType.JWT)
            .build()

        val jwt = SignedJWT(header, claims)
        jwt.sign(ECDSASigner(loadEcPrivateKey(privateKeyPem)))
        return jwt.serialize()
    }

    private fun loadEcPrivateKey(pem: String): ECPrivateKey {
        val clean = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace(Regex("\\s"), "")
        val keyBytes = Base64.getDecoder().decode(clean)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("EC").generatePrivate(keySpec) as ECPrivateKey
    }
}
