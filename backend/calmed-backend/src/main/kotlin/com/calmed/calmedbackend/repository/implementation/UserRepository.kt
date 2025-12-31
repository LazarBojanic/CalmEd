package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.dbQuery
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.UserEntity
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.repository.specification.IUserRepository
import java.util.UUID

class UserRepository : IUserRepository {
	override suspend fun findAll(): List<User> {
		return dbQuery {
			UserEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): User? {
		TODO("Not yet implemented")
	}

	override suspend fun findByEmail(email: String): User? {
		TODO("Not yet implemented")
	}

	override suspend fun create(user: User): User? {
		TODO("Not yet implemented")
	}

	override suspend fun update(user: User): User? {
		TODO("Not yet implemented")
	}

	override suspend fun delete(id: UUID): Boolean {
		TODO("Not yet implemented")
	}
}