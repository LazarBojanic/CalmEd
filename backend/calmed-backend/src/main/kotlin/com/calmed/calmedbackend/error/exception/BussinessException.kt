package com.calmed.calmedbackend.error.exception

import io.ktor.http.*

class BusinessException(
	val statusCode: HttpStatusCode,
	override val message: String
) : RuntimeException(message)