package com.calmed.calmedbackend.model

import kotlinx.serialization.Serializable

@Serializable
sealed class AppResult<out T> {
	@Serializable
	data class Success<out T>(
		val data: T
	) : AppResult<T>()
	@Serializable
	data class Failure(
		val message: String
	) : AppResult<Nothing>()
}