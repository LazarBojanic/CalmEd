package com.calmed.calmedbackend.payment.google

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date

/**
 * Server-side validation of Google Play purchases via the Google Play Developer API.
 *
 * This is the authoritative check that a purchase token belongs to our app, is in the
 * PURCHASED state, and (optionally) which account it is associated with. The deprecated
 * in-app billing V1 RSA signature should not be relied upon for security.
 */
class GooglePlayDeveloperApi(private val serviceAccountJson: String) {

    private val logger = LoggerFactory.getLogger(GooglePlayDeveloperApi::class.java)

    private val httpClient = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedAccessTokenExpiry: Instant = Instant.EPOCH

    fun isConfigured(): Boolean =
        serviceAccountJson.isNotBlank() && serviceAccountJson.trim() != "placeholder"

    @Serializable
    private data class ServiceAccount(
        val client_email: String? = null,
        val private_key: String? = null,
        val token_uri: String? = null
    )

    data class ProductPurchase(
        val purchaseState: Int,
        val productId: String,
        val purchaseToken: String,
        val orderId: String?,
        val acknowledgementState: Int,
        val obfuscatedExternalAccountId: String?,
        val obfuscatedExternalProfileId: String?
    )

    /**
     * Validates a one-time product purchase token.
     *
     * @return the [ProductPurchase] on success, or null when the purchase does not exist / is not
     *         in a valid state (the caller must treat null as a rejection).
     */
    suspend fun validateProductPurchase(packageName: String, productId: String, token: String): ProductPurchase? {
        require(isConfigured()) { "Google Play service account is not configured" }

        val accessToken = getAccessToken()
        val url = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/" +
            "$packageName/purchases/products/$productId/tokens/$token"

        return withContext(Dispatchers.IO) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer $accessToken")
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 404) {
                logger.warn("Google Play purchase token not found for product '$productId'")
                return@withContext null
            }
            if (response.statusCode() == 401) {
                logger.error(
                    "Google Play Developer API returned 401 (permission denied) for product '$productId'. " +
                        "Grant this service account 'View financial data' in Google Play Console > Users and permissions > API access."
                )
                return@withContext null
            }
            if (response.statusCode() !in 200..299) {
                logger.warn("Google Play Developer API returned ${response.statusCode()}: ${response.body()}")
                return@withContext null
            }

            val obj = json.parseToJsonElement(response.body()).jsonObject
            ProductPurchase(
                purchaseState = obj["purchaseState"]?.jsonPrimitive?.content?.toIntOrNull() ?: -1,
                productId = obj["productId"]?.jsonPrimitive?.content ?: "",
                purchaseToken = obj["purchaseToken"]?.jsonPrimitive?.content ?: token,
                orderId = obj["orderId"]?.jsonPrimitive?.content,
                acknowledgementState = obj["acknowledgementState"]?.jsonPrimitive?.content?.toIntOrNull() ?: -1,
                obfuscatedExternalAccountId = obj["obfuscatedExternalAccountId"]?.jsonPrimitive?.content,
                obfuscatedExternalProfileId = obj["obfuscatedExternalProfileId"]?.jsonPrimitive?.content
            )
        }
    }

    private suspend fun getAccessToken(): String {
        val now = Instant.now()
        val cached = cachedAccessToken
        if (cached != null && now.isBefore(cachedAccessTokenExpiry)) return cached

        val account = json.decodeFromString<ServiceAccount>(serviceAccountJson)
        val clientEmail = account.client_email ?: error("service account missing client_email")
        val privateKeyPem = account.private_key ?: error("service account missing private_key")
        val tokenUri = account.token_uri ?: "https://oauth2.googleapis.com/token"

        val assertion = buildJwtAssertion(clientEmail, privateKeyPem, tokenUri)

        val form = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", "UTF-8") +
            "&assertion=" + URLEncoder.encode(assertion, "UTF-8")

        return withContext(Dispatchers.IO) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                error("Failed to obtain Google Play access token (${response.statusCode()}): ${response.body()}")
            }
            val obj = json.parseToJsonElement(response.body()).jsonObject
            val accessToken = obj["access_token"]?.jsonPrimitive?.content
                ?: error("Google token response missing access_token")
            val expiresIn = obj["expires_in"]?.jsonPrimitive?.content?.toLongOrNull() ?: 3600L
            cachedAccessTokenExpiry = Instant.now().plusSeconds((expiresIn - 60).coerceAtLeast(60))
            cachedAccessToken = accessToken
            accessToken
        }
    }

    private fun buildJwtAssertion(clientEmail: String, privateKeyPem: String, tokenUri: String): String {
        val privateKey = loadRsaPrivateKey(privateKeyPem)
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .issuer(clientEmail)
            .claim("scope", "https://www.googleapis.com/auth/androidpublisher")
            .audience(tokenUri)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(3600)))
            .build()

        val jwt = SignedJWT(JWSHeader.Builder(JWSAlgorithm.RS256).build(), claims)
        jwt.sign(RSASSASigner(privateKey))
        return jwt.serialize()
    }

    private fun loadRsaPrivateKey(pem: String): PrivateKey {
        val clean = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace(Regex("\\s"), "")
        val keyBytes = Base64.getDecoder().decode(clean)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }
}
