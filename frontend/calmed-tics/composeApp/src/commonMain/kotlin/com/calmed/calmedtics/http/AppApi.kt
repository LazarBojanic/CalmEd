package com.calmed.calmedtics.http

import com.calmed.calmedtics.model.dto.TokenDto
import com.calmed.calmedtics.model.dto.request.AppleLoginDto
import com.calmed.calmedtics.model.dto.request.ForgotPasswordDto
import com.calmed.calmedtics.model.dto.request.GoogleLoginDto
import com.calmed.calmedtics.model.dto.request.LoginUserDto
import com.calmed.calmedtics.model.dto.request.RefreshDto
import com.calmed.calmedtics.model.dto.request.RegisterUserDto
import com.calmed.calmedtics.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedtics.model.dto.request.SupportMessageRequestDto
import com.calmed.calmedtics.model.dto.request.UserExerciseProgressUpdateDto
import com.calmed.calmedtics.model.dto.request.UserInfoTicsUpdateDto
import com.calmed.calmedtics.model.dto.request.VerifyAppleReceiptDto
import com.calmed.calmedtics.model.dto.request.VerifyGoogleReceiptDto
import com.calmed.calmedtics.model.dto.response.HomeDto
import com.calmed.calmedtics.model.dto.response.MessageDto
import com.calmed.calmedtics.model.dto.response.PaymentStatusDto
import com.calmed.calmedtics.model.dto.response.UserDto
import com.calmed.calmedtics.model.dto.response.UserInfoTicsDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.model.dto.response.SupportMessageResponseDto
import com.calmed.calmedtics.store.ITokenDataStore
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class AppApi(private val appHttpClient: AppHttpClient, private val tokenDataStore: ITokenDataStore
) : IAppApi {
	private val client get() = appHttpClient.client

	override suspend fun register(dto: RegisterUserDto): TokenDto? {
		val resp: HttpResponse = client.post("/auth/register") { setBody(dto) }
		return when (resp.status) {
			HttpStatusCode.Created, HttpStatusCode.OK -> resp.body<TokenDto>()
			else -> null
		}
	}

	override suspend fun login(dto: LoginUserDto): TokenDto? {
		val resp: HttpResponse = client.post("/auth/login") { setBody(dto) }
		return when (resp.status) {
			HttpStatusCode.OK, HttpStatusCode.Created -> resp.body<TokenDto>()
			else -> null
		}
	}

	override suspend fun loginWithGoogle(dto: GoogleLoginDto): TokenDto? {
		try {
			val resp = client.post("/auth/google") { setBody(dto) }
			return when (resp.status) {
				HttpStatusCode.OK, HttpStatusCode.Created -> resp.body<TokenDto>()
				else -> null
			}
		}
		catch (_: Exception) {
			return null
		}
	}
	override suspend fun loginWithApple(dto: AppleLoginDto): TokenDto? {
		return client.post("/auth/apple") {
			setBody(dto)
		}.body()
	}
	override suspend fun refresh(dto: RefreshDto): TokenDto? {
		val resp: HttpResponse = client.post("/auth/refresh") { setBody(dto) }
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<TokenDto>()
			else -> null
		}
	}

	override suspend fun forgotPassword(dto: ForgotPasswordDto): MessageDto? {
		val resp: HttpResponse = client.post("/auth/forgot-password") { setBody(dto) }
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<MessageDto>()
			else -> null
		}
	}

	override suspend fun logout(): MessageDto? {
		return try {
			val resp: HttpResponse = client.post("/auth/logout")
			when (resp.status) {
				HttpStatusCode.OK -> resp.body<MessageDto>()
				else -> null
			}
		}
		catch (_: ClientRequestException) {
			null
		}
		catch (_: Throwable) {
			null
		}
	}

	override suspend fun ping(): String {
		val resp = client.get("/ping")
		return resp.bodyAsText()
	}

	override suspend fun getUser(id: String): UserDto? {
		val resp: HttpResponse = client.get("/user/$id")
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<UserDto>()
			else -> null
		}
	}
	override suspend fun uploadProfileImage(
		imageBytes: ByteArray,
		fileName: String
	): UserDto {
		return client.post("/user/profile-image") {
			setBody(
				MultiPartFormDataContent(
					formData {
						append(
							key = "file",
							value = imageBytes,
							headers = Headers.build {
								append(
									HttpHeaders.ContentType,
									ContentType.Image.JPEG.toString()
								)
								append(
									HttpHeaders.ContentDisposition,
									"filename=\"$fileName\""
								)
							}
						)
					}
				)
			)
		}.body()
	}

	override suspend fun setOnboarded(id: String, dto: SetIsOnboardedDto): UserDto? {
		val resp: HttpResponse = client.post("/user/$id/onboarded") { setBody(dto) }
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<UserDto>()
			else -> null
		}
	}

	override suspend fun deleteAccount(id: String): Boolean {
		val resp: HttpResponse = client.delete("/user/$id")
		return resp.status == HttpStatusCode.OK
	}

	override suspend fun syncExerciseProgress(dto: UserExerciseProgressUpdateDto): Boolean {
		val resp: HttpResponse = client.post("/user-exercise-progress/sync") { setBody(dto) }
		return resp.status == HttpStatusCode.OK
	}

	override suspend fun getUserInfoTicsByUserId(userId: String): UserInfoTicsDto? {
		val resp: HttpResponse = client.get("/user-info-tics/user/$userId")
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<UserInfoTicsDto>()
			else -> null
		}
	}

	override suspend fun updateUserInfoTics(id: String, dto: UserInfoTicsUpdateDto
	): UserInfoTicsDto? {
		val resp: HttpResponse = client.put("/user-info-tics/$id") { setBody(dto) }
		return when (resp.status) {
			HttpStatusCode.OK -> resp.body<UserInfoTicsDto>()
			else -> null
		}
	}



	override suspend fun getHome(year: Int, month: Int): HomeDto? {

		val token = tokenDataStore.tokenDto.first()?.access


		val resp: HttpResponse = client.get("/home") {
			parameter("year", year)
			parameter("month", month)
			token?.let { header("Authorization", "Bearer $it") }

		}

		val raw = resp.bodyAsText()
		return if (resp.status == HttpStatusCode.OK) resp.body() else null

	}

	override suspend fun getAllProgramExercises(): List<ProgramExerciseDto> {
		val token = tokenDataStore.tokenDto.first()?.access

		val resp: HttpResponse = client.get(urlString = "/program-exercises") {
			token?.let { header("Authorization", "Bearer $it") }
		}

		return if (resp.status == HttpStatusCode.OK) resp.body() else emptyList()
	}

	override suspend fun getWelcomeVideo(): ProgramExerciseDto? {
		val token = tokenDataStore.tokenDto.first()?.access
		val resp: HttpResponse = client.get(urlString = "/program-exercises/welcome-video") {
			token?.let { header("Authorization", "Bearer $it") }
		}
		return if (resp.status == HttpStatusCode.OK) resp.body() else null
	}

	override suspend fun getCourseOverviewVideo(): ProgramExerciseDto? {
		val token = tokenDataStore.tokenDto.first()?.access
		val resp: HttpResponse = client.get(urlString = "/program-exercises/course-overview-video") {
			token?.let { header("Authorization", "Bearer $it") }
		}
		return if (resp.status == HttpStatusCode.OK) resp.body() else null
	}

	override suspend fun getPaymentStatus(): PaymentStatusDto? {
		val resp: HttpResponse = client.get("/payment/status")
		return if (resp.status == HttpStatusCode.OK) resp.body() else null
	}


	override suspend fun verifyApplePurchase(dto: VerifyAppleReceiptDto): PaymentStatusDto? {
		val resp: HttpResponse = client.post("/payment/apple/verify") { setBody(dto) }
		return if (resp.status == HttpStatusCode.OK) {
			resp.body()
		} else {
			error("Apple verification failed (${resp.status.value}): ${resp.bodyAsText()}")
		}
	}

	override suspend fun verifyGooglePurchase(dto: VerifyGoogleReceiptDto): PaymentStatusDto? {
		val resp: HttpResponse = client.post("/payment/google/verify") { setBody(dto) }
		return if (resp.status == HttpStatusCode.OK) {
			resp.body()
		} else {
			error("Google verification failed (${resp.status.value}): ${resp.bodyAsText()}")
		}
	}
	override suspend fun sendSupportMessage(
		request: SupportMessageRequestDto
	): SupportMessageResponseDto {
		val resp: HttpResponse = client.post("/support/message") {
			setBody(request)
		}

		return when (resp.status) {
			HttpStatusCode.OK -> resp.body()
			else -> error("Support message failed (${resp.status.value}): ${resp.bodyAsText()}")
		}
	}
}

