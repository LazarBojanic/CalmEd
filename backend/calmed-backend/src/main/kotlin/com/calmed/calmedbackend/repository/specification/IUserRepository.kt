package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.model.raw.user.PaymentType
import java.util.UUID

interface IUserRepository {
	suspend fun findAll(): List<User>
	suspend fun findById(id: UUID): User?
	suspend fun findByEmail(email: String): User?
	suspend fun create(user: User): User?
	suspend fun update(user: User): User?
	suspend fun delete(id: UUID): Boolean
 suspend fun setIsOnboarded(id: UUID, isOnboarded: Boolean): User?

}
