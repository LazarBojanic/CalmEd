package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.model.MapMode
import com.calmed.calmedbackend.model.raw.userprogram.UserProgram
import com.calmed.calmedbackend.model.raw.userprogram.UserProgramEntity
import com.calmed.calmedbackend.model.raw.userprogram.UserProgramTable
import com.calmed.calmedbackend.model.setFrom
import com.calmed.calmedbackend.model.toRaw
import com.calmed.calmedbackend.repository.specification.IUserProgramRepository
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class UserProgramRepository : IUserProgramRepository {
	override suspend fun findAll(): List<UserProgram> =
		UserProgramEntity.all().map { it.toRaw() }

	override suspend fun findById(id: UUID): UserProgram? =
		UserProgramEntity.findById(id)?.toRaw()

	override suspend fun findByUserId(userId: UUID): UserProgram? =
		UserProgramEntity.find { UserProgramTable.userId eq userId }.firstOrNull()?.toRaw()

	override suspend fun create(userProgram: UserProgram): UserProgram? {
		if (UserProgramEntity.findById(userProgram.id) != null) return null
		return UserProgramEntity.new(userProgram.id) { setFrom(userProgram, MapMode.CREATE) }.toRaw()
	}

	override suspend fun update(userProgram: UserProgram): UserProgram? {
		val e = UserProgramEntity.findById(userProgram.id) ?: return null
		e.setFrom(userProgram, MapMode.UPDATE)
		return e.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean {
		val e = UserProgramEntity.findById(id) ?: return false
		e.delete()
		return true
	}

	override suspend fun deleteByUserId(userId: UUID): Boolean {
		val entities = UserProgramEntity.find { UserProgramTable.userId eq userId }
		var deleted = false
		for (e in entities) {
			e.delete()
			deleted = true
		}
		return deleted
	}
}
