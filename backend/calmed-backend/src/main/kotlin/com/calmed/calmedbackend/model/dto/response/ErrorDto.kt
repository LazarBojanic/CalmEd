package com.calmed.calmedbackend.model.dto.response

import com.calmed.calmedbackend.util.HttpStatusCodeSerializer
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
	val error: String,
)