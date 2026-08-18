package com.calmed.calmedbackend.repository.specification

import com.calmed.calmedbackend.model.raw.userprogram.UserProgram
import java.util.UUID

interface IUserProgramRepository {
	suspend fun findAll(): List<UserProgram>
	suspend fun findById(id: UUID): UserProgram?
	suspend fun findByUserId(userId: UUID): UserProgram?
	suspend fun create(userProgram: UserProgram): UserProgram?
	suspend fun update(userProgram: UserProgram): UserProgram?
	suspend fun delete(id: UUID): Boolean
	suspend fun deleteByUserId(userId: UUID): Boolean
}