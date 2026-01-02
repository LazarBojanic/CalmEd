package com.calmed.calmedbackend.model

sealed class AppResult<out T> {

	data class Success<out T>(
		val data: T
	) : AppResult<T>()

	data class Failure(
		val message: String
	) : AppResult<Nothing>()
}