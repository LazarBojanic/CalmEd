package com.calmed.calmedbackend.service.implementation

import com.calmed.calmedbackend.model.join
import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.user.User
import com.calmed.calmedbackend.repository.specification.IUserRepository
import com.calmed.calmedbackend.service.specification.IUserService
import java.util.UUID

class UserService(
	private val userRepository: IUserRepository
) : IUserService {

	override suspend fun getAll(): List<UserJoined> {
		return userRepository.findAll().map { it.join() }
	}

	override suspend fun getById(id: UUID): UserJoined? {
		val user = userRepository.findById(id) ?: return null
		return user.join()
	}

	override suspend fun getByEmail(email: String): UserJoined? {
		val user = userRepository.findByEmail(email) ?: return null
		return user.join()
	}

	override suspend fun create(user: User): UserJoined? {
		val created = userRepository.create(user) ?: return null
		return created.join()
	}

	override suspend fun update(user: User): UserJoined? {
		val updated = userRepository.update(user) ?: return null
		return updated.join()
	}

	override suspend fun delete(id: UUID): Boolean {
		return userRepository.delete(id)
	}
}