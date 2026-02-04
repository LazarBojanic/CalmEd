package com.calmed.calmedbackend.repository.implementation

import com.calmed.calmedbackend.database.withTransaction
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
	override suspend fun findAll(): List<UserProgram> = withTransaction {
		UserProgramEntity.all().map { it.toRaw() }
	}

	override suspend fun findById(id: UUID): UserProgram? = withTransaction {
		UserProgramEntity.findById(id)?.toRaw()
	}

	override suspend fun findByUserId(userId: UUID): UserProgram? = withTransaction {
		UserProgramEntity.find { UserProgramTable.userId eq userId }.firstOrNull()?.toRaw()
	}

	override suspend fun create(userProgram: UserProgram): UserProgram? = withTransaction {
		val existing = UserProgramEntity.findById(userProgram.id)
		if (existing == null) {
			UserProgramEntity.new(userProgram.id) { setFrom(userProgram, MapMode.CREATE) }.toRaw()
		} else null
	}

	override suspend fun update(userProgram: UserProgram): UserProgram? = withTransaction {
		val e = UserProgramEntity.findById(userProgram.id)
		e?.apply { setFrom(userProgram, MapMode.UPDATE) }?.toRaw()
	}

	override suspend fun delete(id: UUID): Boolean = withTransaction {
		UserProgramEntity.findById(id)?.let { it.delete(); true } ?: false
	}
}