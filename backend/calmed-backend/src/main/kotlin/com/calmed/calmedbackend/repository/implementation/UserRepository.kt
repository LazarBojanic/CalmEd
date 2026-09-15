package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
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
		return UserEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): User? {
		return UserEntity.findById(id)?.toRaw()
	}

	override suspend fun findByEmail(email: String): User? {
		return UserEntity
			.find { UserTable.email eq email }
			.firstOrNull()
			?.toRaw()
	}

	override suspend fun create(user: User): User? {
		if (UserEntity.findById(user.id) != null) return null
		return UserEntity.new(user.id) {
			setFrom(user, MapMode.CREATE)
		}.toRaw()
	}

	override suspend fun update(user: User): User? {
		val e = UserEntity.findById(user.id) ?: return null
		e.setFrom(user, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = UserEntity.findById(id) ?: return false
		e.delete()
		return true
	}

	override suspend fun setIsOnboarded(id: UUID, isOnboarded: Boolean): User? {
		val e = UserEntity.findById(id) ?: return null
		e.isOnboarded = isOnboarded
		e.updatedAt = Instant.now()
		return e.toRaw()
	}

	override suspend fun setConfirmOverEighteen(id: UUID, confirmOverEighteen: Boolean): User? {
		val e = UserEntity.findById(id) ?: return null
		e.confirmOverEighteen = confirmOverEighteen
		e.updatedAt = Instant.now()
		return e.toRaw()
	}
}
