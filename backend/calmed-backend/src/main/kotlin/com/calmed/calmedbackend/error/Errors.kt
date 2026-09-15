package com.calmed.calmedbackend.error

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.calmed.calmedbackend.error.Errors")

fun Application.configureStatusPages() {
	install(StatusPages) {
		exception<RequestValidationException> { call, cause ->
			logger.error("Request validation failed", cause)
			call.respond(
				HttpStatusCode.BadRequest,
				AppResult.Failure(HttpStatusCode.BadRequest, cause.reasons.joinToString(" "))
			)
		}

		exception<BusinessException> { call, cause ->
			logger.error("Business exception", cause)
			call.respond(
				cause.statusCode,
				AppResult.Failure(cause.statusCode, cause.message)
			)
		}

		exception<Throwable> { call, cause ->
			logger.error("Unexpected error occurred", cause)
			call.respond(
				HttpStatusCode.InternalServerError,
				AppResult.Failure(HttpStatusCode.InternalServerError, "Unexpected error occurred")
			)
		}
	}
}