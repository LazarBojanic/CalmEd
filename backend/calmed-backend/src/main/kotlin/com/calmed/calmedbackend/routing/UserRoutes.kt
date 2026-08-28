package com.calmed.calmedbackend.routing

import com.calmed.calmedbackend.error.exception.BusinessException
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.dto.request.SetConfirmOverEighteenDto
import com.calmed.calmedbackend.model.dto.request.SetIsOnboardedDto
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.toDto
import com.calmed.calmedbackend.service.specification.IAccountDeletionService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.readAvailable
import org.koin.ktor.ext.inject
import java.io.File
import java.util.UUID

fun Route.userRoutes() {
	val userService by inject<IUserService>()
	val accountDeletionService by inject<IAccountDeletionService>()

	authenticate("auth-jwt") {

		route("/user") {

			get("/me") {
				val jwt = call.principal<JWTPrincipal>()
					?: throw BusinessException(
						HttpStatusCode.Unauthorized,
						"Invalid authentication"
					)

				val id = UUID.fromString(jwt.subject)

				println("[DEBUG_LOG] UserRoutes: /me called for userId: $id")

				val res = userService.getById(id)

				when (res) {
					is AppResult.Success -> {
						println(
							"[DEBUG_LOG] UserRoutes: /me success for userId: $id, onboarded: ${res.data.isOnboarded}"
						)

						call.respond(
							HttpStatusCode.OK,
							res.data.toDto()
						)
					}

					is AppResult.Failure -> {
						println(
							"[DEBUG_LOG] UserRoutes: /me FAILURE for userId: $id, error: ${res.message}"
						)

						call.respond(
							res.httpStatusCode,
							res.message
						)
					}
				}
			}

			post("/profile-image") {

				val jwt = call.principal<JWTPrincipal>()
					?: throw BusinessException(
						HttpStatusCode.Unauthorized,
						"Invalid authentication"
					)

				val userId = UUID.fromString(jwt.subject)

				val multipart = call.receiveMultipart()

				var savedFileName: String? = null

				multipart.forEachPart { part ->

					if (part is PartData.FileItem) {

						val originalFileName =
							part.originalFileName ?: "profile.jpg"

						val extension =
							originalFileName
								.substringAfterLast('.', "jpg")
								.lowercase()

						val allowedExtensions =
							setOf("jpg", "jpeg", "png", "webp")

						if (extension !in allowedExtensions) {
							part.dispose()

							throw BusinessException(
								HttpStatusCode.BadRequest,
								"Unsupported image format"
							)
						}

						val uploadsDir =
							File("uploads/profile")

						if (!uploadsDir.exists()) {
							uploadsDir.mkdirs()
						}

						val fileName =
							"${userId}_${System.currentTimeMillis()}.$extension"

						val file =
							File(
								uploadsDir,
								fileName
							)

						val channel = part.provider()

						file.outputStream().buffered().use { output ->
							val buffer = ByteArray(8192)

							while (!channel.isClosedForRead) {
								val read = channel.readAvailable(buffer)

								if (read > 0) {
									output.write(buffer, 0, read)
								}
							}
						}

						savedFileName = fileName
					}

					part.dispose()
				}

				if (savedFileName == null) {
					throw BusinessException(
						HttpStatusCode.BadRequest,
						"Image file is missing"
					)
				}

				val profileImageUrl =
					"/uploads/profile/$savedFileName"

				val result =
					userService.updateProfileImage(
						userId = userId,
						profileImageUrl = profileImageUrl
					)

				when (result) {
					is AppResult.Success<UserJoined> -> {
						call.respond(
							HttpStatusCode.OK,
							result.data.toDto()
						)
					}

					is AppResult.Failure -> {
						call.respond(
							result.httpStatusCode,
							result.message
						)
					}
				}
			}

			get("/{id}") {
				val idParam = call.parameters["id"]

				if (idParam != null) {
					val id = UUID.fromString(idParam)

					val res = userService.getById(id)

					when (res) {
						is AppResult.Success -> {
							call.respond(
								HttpStatusCode.OK,
								res.data.toDto()
							)
						}

						is AppResult.Failure -> {
							call.respond(
								res.httpStatusCode,
								res.message
							)
						}
					}
			} else {
				throw BusinessException(
					HttpStatusCode.BadRequest,
					"Missing id parameter"
				)
			}
		}

		post("/{id}/onboarded") {
			val idParam = call.parameters["id"]

			if (idParam == null) {
				throw BusinessException(
					HttpStatusCode.BadRequest,
					"Missing id parameter"
				)
			}

			val id = UUID.fromString(idParam)

			val jwt = call.principal<JWTPrincipal>()
				?: throw BusinessException(
					HttpStatusCode.Unauthorized,
					"Invalid authentication"
				)

			val subjectId = UUID.fromString(jwt.subject)

			if (subjectId != id) {
				throw BusinessException(
					HttpStatusCode.Forbidden,
					"Forbidden"
				)
			}

			val dto = call.receive<SetIsOnboardedDto>()

			val res = userService.setIsOnboarded(
				id,
				dto.isOnboarded
			)

			when (res) {
				is AppResult.Success -> {
					call.respond(
						HttpStatusCode.OK,
						res.data.toDto()
					)
				}

				is AppResult.Failure -> {
					call.respond(
						res.httpStatusCode,
						res.message
					)
				}
			}
		}

		post("/{id}/confirm-age") {
			val idParam = call.parameters["id"]

			if (idParam == null) {
				throw BusinessException(
					HttpStatusCode.BadRequest,
					"Missing id parameter"
				)
			}

			val id = UUID.fromString(idParam)

			val jwt = call.principal<JWTPrincipal>()
				?: throw BusinessException(
					HttpStatusCode.Unauthorized,
					"Invalid authentication"
				)

			val subjectId = UUID.fromString(jwt.subject)

			if (subjectId != id) {
				throw BusinessException(
					HttpStatusCode.Forbidden,
					"Forbidden"
				)
			}

			val dto = call.receive<SetConfirmOverEighteenDto>()

			val res = userService.setConfirmOverEighteen(
				id,
				dto.confirmOverEighteen
			)

			when (res) {
				is AppResult.Success -> {
					call.respond(
						HttpStatusCode.OK,
						res.data.toDto()
					)
				}

				is AppResult.Failure -> {
					call.respond(
						res.httpStatusCode,
						res.message
					)
				}
			}
		}

		delete("/{id}") {
				val idParam = call.parameters["id"]

				if (idParam == null) {
					throw BusinessException(
						HttpStatusCode.BadRequest,
						"Missing id parameter"
					)
				}

				val id = UUID.fromString(idParam)

				val jwt = call.principal<JWTPrincipal>()
					?: throw BusinessException(
						HttpStatusCode.Unauthorized,
						"Invalid authentication"
					)

				val subjectId = UUID.fromString(jwt.subject)

				if (subjectId != id) {
					throw BusinessException(
						HttpStatusCode.Forbidden,
						"Forbidden"
					)
				}

				when (val res = accountDeletionService.deleteAccount(id)) {
					is AppResult.Success -> {
						call.respond(
							HttpStatusCode.OK,
							mapOf(
								"message" to "Account deleted."
							)
						)
					}

					is AppResult.Failure -> {
						call.respond(
							res.httpStatusCode,
							res.message
						)
					}
				}
			}
		}
	}
}