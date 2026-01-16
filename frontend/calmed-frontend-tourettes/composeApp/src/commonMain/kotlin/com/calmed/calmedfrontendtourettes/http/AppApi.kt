package com.calmed.calmedfrontendtourettes.http

import com.calmed.calmedfrontendtourettes.model.dto.TokenDto
import com.calmed.calmedfrontendtourettes.model.dto.request.ForgotPasswordDto
import com.calmed.calmedfrontendtourettes.model.dto.request.GoogleLoginDto
import com.calmed.calmedfrontendtourettes.model.dto.request.LoginUserDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RefreshDto
import com.calmed.calmedfrontendtourettes.model.dto.request.RegisterUserDto
import com.calmed.calmedfrontendtourettes.model.dto.response.MessageDto
import com.calmed.calmedfrontendtourettes.store.ITokenDataStore
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class AppApi(
    private val appHttpClient: AppHttpClient,
    private val tokenDataStore: ITokenDataStore
) : IAppApi {
    private val client get() = appHttpClient.client

    override suspend fun register(dto: RegisterUserDto): TokenDto? {
        val resp: HttpResponse = client.post("/auth/register") {
            setBody(dto)
        }
        return when (resp.status) {
            HttpStatusCode.Created, HttpStatusCode.OK -> {
                resp.body<TokenDto>()
            }
            else -> {
                null
            }
        }
    }

    override suspend fun login(dto: LoginUserDto): TokenDto? {
        val resp: HttpResponse = client.post("/auth/login") {
            setBody(dto)
        }
        return when (resp.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> {
                resp.body<TokenDto>()
            }
            else -> {
                null
            }
        }
    }

    override suspend fun loginWithGoogle(dto: GoogleLoginDto): TokenDto? {
        println("API: POST /auth/google")
        try {

            val resp = client.post("/auth/google") {
                setBody(dto)
            }
            println("API: status = ${resp.status}")

            return when (resp.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> resp.body<TokenDto>()
                else -> {
                    println("API: non-OK body = ${resp.bodyAsText()}")
                    null
                }
            }
        } catch (e: Exception) {
            println("API: loginWithGoogle EX: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    override suspend fun refresh(dto: RefreshDto): TokenDto? {
        val resp: HttpResponse = client.post("/auth/refresh") {
            setBody(dto)
        }
        return when (resp.status) {
            HttpStatusCode.OK -> {
                resp.body<TokenDto>()
            }
            else -> {
                null
            }
        }
    }

    override suspend fun forgotPassword(dto: ForgotPasswordDto): MessageDto? {
        val resp: HttpResponse = client.post("/auth/forgot-password") {
            setBody(dto)
        }
        return when (resp.status) {
            HttpStatusCode.OK -> {
                resp.body<MessageDto>()
            }
            else -> {
                null
            }
        }
    }

    override suspend fun logout(): MessageDto? {
        return try {
            val resp: HttpResponse = client.post("/auth/logout")
            when (resp.status) {
                HttpStatusCode.OK -> {
                    resp.body<MessageDto>()
                }
                else -> {
                    null
                }
            }
        } catch (e: ClientRequestException) {
            null
        } catch (e: Throwable) {
            null
        }
    }

    override suspend fun ping(): String {
        val resp = client.get("/ping")
        return resp.bodyAsText()
    }
}