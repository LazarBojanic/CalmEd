package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.database.withResultTransaction
import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.AppResult
import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserProgramJoined
import com.calmed.calmedbackend.model.raw.userprogram.UserProgram
import com.calmed.calmedbackend.repository.specification.IUserProgramRepository
import com.calmed.calmedbackend.service.specification.IUserProgramService
import com.calmed.calmedbackend.service.specification.IUserService
import io.ktor.http.HttpStatusCode
import java.util.UUID

class UserProgramService(
	private val repository: IUserProgramRepository,
	private val userService: IUserService
) : IUserProgramService {
	override suspend fun getAll(): AppResult<List<UserProgramJoined>> {
		return withTransaction {
			val usersById = when (val usersResult = userService.getAll()) {
				is AppResult.Success -> usersResult.data.associateBy { it.id }
				is AppResult.Failure -> return@withTransaction AppResult.Failure(
					usersResult.httpStatusCode, "Failed to retrieve users. ${usersResult.message}"
				)
			}
			val result = mutableListOf<UserProgramJoined>()
			for (raw in repository.findAll()) {
				val user = usersById[raw.userId]
					?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "Failed to retrieve user.")
				result.add(raw.join(user))
			}
			AppResult.Success(result)
		}
	}

	override suspend fun getById(id: UUID): AppResult<UserProgramJoined> {
		return withTransaction {
			val raw = repository.findById(id) ?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "User program not found.")
			when (val u = userService.getById(raw.userId)) {
				is AppResult.Success -> AppResult.Success(raw.join(u.data))
				is AppResult.Failure -> AppResult.Failure(u.httpStatusCode, "Failed to retrieve user. ${u.message}")
			}
		}
	}

	override suspend fun getByUserId(userId: UUID): AppResult<UserProgramJoined> {
		return withTransaction {
			val raw = repository.findByUserId(userId) ?: return@withTransaction AppResult.Failure(HttpStatusCode.NotFound, "User program not found.")
			when (val u = userService.getById(raw.userId)) {
				is AppResult.Success -> AppResult.Success(raw.join(u.data))
				is AppResult.Failure -> AppResult.Failure(u.httpStatusCode, "Failed to retrieve user. ${u.message}")
			}
		}
	}

	override suspend fun create(userProgram: UserProgram): AppResult<UserProgramJoined> {
		return withResultTransaction {
			val created = repository.create(userProgram) ?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to create user program.")
			when (val u = userService.getById(created.userId)) {
				is AppResult.Success -> AppResult.Success(created.join(u.data))
				is AppResult.Failure -> AppResult.Failure(u.httpStatusCode, "Failed to retrieve user. ${u.message}")
			}
		}
	}

	override suspend fun update(userProgram: UserProgram): AppResult<UserProgramJoined> {
		return withResultTransaction {
			val updated = repository.update(userProgram) ?: return@withResultTransaction AppResult.Failure(HttpStatusCode.BadRequest, "Failed to update user program.")
			when (val u = userService.getById(updated.userId)) {
				is AppResult.Success -> AppResult.Success(updated.join(u.data))
				is AppResult.Failure -> AppResult.Failure(u.httpStatusCode, "Failed to retrieve user. ${u.message}")
			}
		}
	}

	override suspend fun delete(id: UUID): AppResult<Unit> {
		return withResultTransaction {
			if (repository.delete(id)) AppResult.Success(Unit) else AppResult.Failure(HttpStatusCode.NotFound, "Failed to delete user program.")
		}
	}
}
