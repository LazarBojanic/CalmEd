package com.calmed.calmedtics.http

import com.calmed.calmedtics.model.dto.TokenDto
import com.calmed.calmedtics.model.dto.request.RefreshDto
import com.calmed.calmedtics.store.ITokenDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

private class RefreshTokenConfig {
	var tokenProvider: () -> String? = { null }
	var refresh: suspend () -> String? = { null }
}

private val RefreshTokenPlugin = createClientPlugin("RefreshTokenPlugin", ::RefreshTokenConfig) {
	on(Send) { request ->
		var hadToken = false
		val token = pluginConfig.tokenProvider()
		if (!token.isNullOrBlank()) {
			hadToken = true
			request.headers.remove(HttpHeaders.Authorization)
			request.headers.append(HttpHeaders.Authorization, "Bearer $token")
		}

		var call = proceed(request)

		if (hadToken && call.response.status == HttpStatusCode.Unauthorized) {
			val newToken = pluginConfig.refresh()
			if (!newToken.isNullOrBlank()) {
				val retry = HttpRequestBuilder().takeFrom(request)
				retry.headers.remove(HttpHeaders.Authorization)
				retry.headers.append(HttpHeaders.Authorization, "Bearer $newToken")
				call = proceed(retry)
			}
		}
		call
	}
}

class AppHttpClient(
	val baseUrl: String,
	val platformEngine: HttpClientEngineFactory<*>,
	private val tokenStore: ITokenDataStore,
	enableLogging: Boolean = false
) {

	private val json = Json {
		ignoreUnknownKeys = true
		isLenient = true
		explicitNulls = false
	}

	private val refreshMutex = Mutex()

	private val refreshClient = HttpClient(platformEngine) {
		install(ContentNegotiation) { json(json) }
		install(HttpTimeout) {
			requestTimeoutMillis = 30_000
			connectTimeoutMillis = 30_000
			socketTimeoutMillis = 30_000
		}
		expectSuccess = false
	}

	private suspend fun refreshToken(): String? = refreshMutex.withLock {
		val current = tokenStore.getToken() ?: return@withLock null
		val refresh = current.refresh
		if (refresh.isNullOrBlank()) return@withLock null

		val response = refreshClient.post("$baseUrl/auth/refresh") {
			contentType(ContentType.Application.Json)
			setBody(RefreshDto(refresh = refresh))
		}
		if (response.status != HttpStatusCode.OK) {
			tokenStore.clear()
			return@withLock null
		}

		val token: TokenDto = response.body()
		tokenStore.setToken(token)
		token.access
	}

	val client: HttpClient = HttpClient(platformEngine) {
		install(ContentNegotiation) { json(json) }

		if (enableLogging) {
			install(Logging) {
				level = LogLevel.HEADERS
			}
		}

		install(HttpTimeout) {
			requestTimeoutMillis = 30_000
			connectTimeoutMillis = 30_000
			socketTimeoutMillis = 30_000
		}

		install(DefaultRequest) {
			url { takeFrom(baseUrl) }

			headers.append("User-Agent", "CalmEd")

			contentType(ContentType.Application.Json)
			accept(ContentType.Application.Json)
		}

		install(RefreshTokenPlugin) {
			tokenProvider = { tokenStore.tokenDto.value?.access }
			refresh = { refreshToken() }
		}
	}
}
