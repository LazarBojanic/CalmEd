package com.calmed.calmedbackend.model.raw.userprogram

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

class UserProgramEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<UserProgramEntity>(UserProgramTable)
	var userId by UserProgramTable.userId
	var startDate by UserProgramTable.startDate
	var endDate by UserProgramTable.endDate
	var timezone by UserProgramTable.timezone
	var createdAt by UserProgramTable.createdAt
	var updatedAt by UserProgramTable.updatedAt
}
