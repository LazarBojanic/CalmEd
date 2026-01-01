package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.joined.UserJoined
import com.calmed.calmedbackend.model.raw.user.User
import java.util.UUID

interface IUserService {
	suspend fun getAll(): List<UserJoined>
	suspend fun getById(id: UUID): UserJoined?
	suspend fun getByEmail(email: String): UserJoined?
	suspend fun create(user: User): UserJoined?
	suspend fun update(user: User): UserJoined?
	suspend fun delete(id: UUID): Boolean
}