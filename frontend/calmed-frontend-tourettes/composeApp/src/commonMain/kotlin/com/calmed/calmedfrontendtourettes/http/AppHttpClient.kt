package com.calmed.calmedfrontendtourettes.http

import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AppHttpClient(
    val baseUrl: String,
    val platformEngine: HttpClientEngineFactory<*>,
    private val tokenStore: ITokenDataStore
) {
    init {
        println("HTTP baseUrl = $baseUrl")
    }
    val client: HttpClient = HttpClient(platformEngine) {

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                    prettyPrint = true
                }
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
        install(DefaultRequest) {
            println("HTTP baseUrl = $baseUrl")
            url {
                takeFrom(baseUrl)
            }
            println("HTTP full url = ${url.buildString()}")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            val token = tokenStore.tokenDto.value
            if (token != null) {
                val access = token.access
                if (access != null) {
                    if (access.isNotBlank()) {
                        header(HttpHeaders.Authorization, "Bearer $access")
                    }
                }
            }
        }
    }
}