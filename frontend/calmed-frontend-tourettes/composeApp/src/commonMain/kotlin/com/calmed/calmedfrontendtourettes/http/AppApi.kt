package com.calmed.calmedfrontendtourettes.http

import com.calmed.calmedfrontendtourettes.model.dto.TokenDto
import com.calmed.calmedfrontendtourettes.model.dto.request.LoginUserDto
import com.calmed.calmedfrontendtourettes.model.dto.response.MessageDto
import com.calmed.calmedfrontendtourettes.model.raw.Message
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters

class AppApi(
	private val appHttpClient: AppHttpClient,
	private val tokenDataStore: ITokenDataStore
) : IAppApi {
	private val client get() = appHttpClient.client

	private suspend inline fun HttpResponse.bodyOrNullAsText(): String? =
		when (status) {
			HttpStatusCode.OK, HttpStatusCode.Created, HttpStatusCode.Accepted -> try {
				body<String>()
			}
			catch (_: Throwable) {
				null
			}

			else -> null
		}

	override suspend fun login(loginUserDto: LoginUserDto): TokenDto? {
		val resp: HttpResponse = client.post("/auth/login") { setBody(loginUserDto) }
		return when (resp.status) {
			HttpStatusCode.OK, HttpStatusCode.Created -> resp.body<TokenDto>()
			else -> null
		}
	}

	override suspend fun forgotPassword(email: String): String? {
		val resp: HttpResponse = client.post("/TODO") { url { parameters.append("email", email) } }
		return resp.bodyOrNullAsText()
	}

	override suspend fun logout(): Boolean {
		val resp: HttpResponse = client.post("/auth/logout")
		return resp.status == HttpStatusCode.OK
	}

	override suspend fun refreshToken(): TokenDto? {
		val tokenDto = tokenDataStore.tokenDto.value
		val resp: HttpResponse = client.post("/auth/refresh") { setBody(tokenDto) }
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<TokenDto>()
			else -> null
		}
	}

	override suspend fun getAllMessages(): List<MessageDto> {
		val resp: HttpResponse = client.get("/message/get-all")
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<List<MessageDto>>()
			else -> emptyList()
		}

	}

	override suspend fun getMessageById(id: String): MessageDto? {
		TODO("Not yet implemented")
	}

	override suspend fun createMessage(message: Message): MessageDto? {
		TODO("Not yet implemented")
	}

	override suspend fun updateMessage(message: Message): MessageDto? {
		TODO("Not yet implemented")
	}

	override suspend fun deleteMessage(id: String): Boolean {
		TODO("Not yet implemented")
	}
}