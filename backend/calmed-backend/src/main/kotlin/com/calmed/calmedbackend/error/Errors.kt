package com.calmed.calmedbackend.error

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.util.Util.Companion.printError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
	install(StatusPages) {
		exception<BusinessException> { call, cause ->
			printError(cause)
			call.respond(
				cause.statusCode,
				AppResult.Failure(cause.message)
			)
		}

		exception<Throwable> { call, cause ->
			printError(cause)
			call.respond(
				HttpStatusCode.InternalServerError,
				AppResult.Failure("Unexpected error occurred")
			)
		}
	}
}