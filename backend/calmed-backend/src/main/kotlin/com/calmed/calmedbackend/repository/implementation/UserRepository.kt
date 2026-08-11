package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.user.PaymentType
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.UserEntity
import com.calmed.calmedbackend.model.raw.user.UserTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserRepository
import org.jetbrains.exposed.v1.core.eq
import java.time.Instant
import java.util.UUID

class UserRepository : IUserRepository {

	override suspend fun findAll(): List<User> {
		return withTransaction {
			UserEntity.all().map { it.toRaw() }
		}
	}

	override suspend fun findById(id: UUID): User? {
		return withTransaction {
			val e = UserEntity.findById(id)
			if (e != null) {
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun findByEmail(email: String): User? {
		return withTransaction {
			val e = UserEntity
				.find { UserTable.email eq email }
				.firstOrNull()

			if (e != null) {
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun create(user: User): User? {
		return withTransaction {
			val existing = UserEntity.findById(user.id)
			if (existing == null) {
				return@withTransaction UserEntity.new(user.id) {
					setFrom(user, MapMode.CREATE)
				}.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun update(user: User): User? {
		return withTransaction {
			val e = UserEntity.findById(user.id)
			if (e != null) {
				e.setFrom(user, MapMode.UPDATE)
				return@withTransaction e.toRaw()
			}
			else {
				return@withTransaction null
			}
		}
	}

	override suspend fun delete(id: UUID): Boolean {
		return withTransaction {
			val e = UserEntity.findById(id)
			if (e != null) {
				e.delete()
				return@withTransaction true
			}
			else {
				return@withTransaction false
			}
		}
	}

	override suspend fun setIsOnboarded(id: UUID, isOnboarded: Boolean
	): User? {
		return withTransaction {
			val e = UserEntity.findById(id)
			if(e != null) {
				e.isOnboarded = isOnboarded
				e.updatedAt = Instant.now()
				return@withTransaction e.toRaw()
			}
			else{
				return@withTransaction null
			}
		}
	}

}
