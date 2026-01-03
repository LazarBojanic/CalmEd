package com.calmed.calmedbackend.model

import com.calmed.calmedbackend.util.HttpStatusCodeSerializer
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
sealed class AppResult<out T> {
	@Serializable
	data class Success<out T>(
		val data: T
	) : AppResult<T>()
	@Serializable
	data class Failure(
		@Serializable(with = HttpStatusCodeSerializer::class)
		val httpStatusCode: HttpStatusCode,
		val message: String
	) : AppResult<Nothing>()
}