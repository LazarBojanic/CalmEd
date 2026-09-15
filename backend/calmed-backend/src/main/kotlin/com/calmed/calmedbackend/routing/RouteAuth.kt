package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.requireSubjectId(): UUID {
	val jwt = principal<JWTPrincipal>()
		?: throw BusinessException(HttpStatusCode.Unauthorized, "Invalid authentication")
	return try {
		UUID.fromString(jwt.subject)
	} catch (e: Exception) {
		throw BusinessException(HttpStatusCode.Unauthorized, "Invalid authentication")
	}
}

fun ApplicationCall.requireSelf(id: UUID) {
	val subjectId = requireSubjectId()
	println("subjectId: $subjectId")
	println("idParam: $id")
	if (!subjectId.equals(id)) {
		throw BusinessException(HttpStatusCode.Forbidden, "Forbidden")
	}
}
