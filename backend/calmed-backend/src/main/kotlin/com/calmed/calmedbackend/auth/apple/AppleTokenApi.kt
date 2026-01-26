package com.calmed.calmedbackend.auth.apple

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

data class AppleTokenResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val refresh_token: String? = null,
    val id_token: String? = null,
    val error: String? = null,
    val error_description: String? = null
)

class AppleTokenApi(
    private val http: HttpClient,
    private val config: AppleConfig
) {
    suspend fun exchangeCode(code: String, redirectUri: String): AppleTokenResponse {
        val clientSecret = AppleClientSecret.generate(config)

        return http.post("https://appleid.apple.com/auth/token") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("client_id", config.clientId)
                    append("client_secret", clientSecret)
                    append("redirect_uri", redirectUri)
                }.formUrlEncode()
            )
        }.body()
    }
}
