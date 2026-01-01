package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.service.specification.IUserService
import java.util.UUID

class UserService : IUserService {
	override suspend fun getAll(): List<UserJoined> {
		TODO("Not yet implemented")
	}

	override suspend fun getById(id: UUID): UserJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun getByEmail(email: String): UserJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun create(user: User): UserJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun update(user: User): UserJoined? {
		TODO("Not yet implemented")
	}

	override suspend fun delete(id: UUID): Boolean {
		TODO("Not yet implemented")
	}
}