package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.tx
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.UserEntity
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserRepository
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class UserRepository : IUserRepository {

	override suspend fun findAll(): List<User> {
		return tx {
			UserEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): User? {
		return tx {
			val e = UserEntity.findById(id)
			if (e != null) {
				return@tx e.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun findByEmail(email: String): User? {
		return tx {
			val e = UserEntity
				.find { UserTable.email eq email }
				.firstOrNull()

			if (e != null) {
				return@tx e.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun create(user: User): User? {
		return tx {
			val existing = UserEntity.findById(user.id)
			if (existing == null) {
				return@tx UserEntity.new(user.id) {
					setFrom(user, MapMode.CREATE)
				}.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun update(user: User): User? {
		return tx {
			val e = UserEntity.findById(user.id)
			if (e != null) {
				e.setFrom(user, MapMode.UPDATE)
				return@tx e.toRaw()
			}
			else {
				return@tx null
			}
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return tx {
			val e = UserEntity.findById(id)
			if (e != null) {
				e.delete()
				return@tx true
			}
			else {
				return@tx false
			}
		}
	}
}
