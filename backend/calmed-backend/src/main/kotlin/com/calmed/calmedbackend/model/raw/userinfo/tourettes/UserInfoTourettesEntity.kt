package com.calmed.calmedbackend.model.raw.userinfo.tics

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.*

class UserInfoTicsEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<UserInfoTicsEntity>(UserInfoTicsTable)
	var userId by UserInfoTicsTable.userId
	var preferredName by UserInfoTicsTable.preferredName
	var age by UserInfoTicsTable.age
	var stressLevel by UserInfoTicsTable.stressLevel
	var tickType by UserInfoTicsTable.tickType
	var tickFrequency by UserInfoTicsTable.tickFrequency
	var ticDuration by UserInfoTicsTable.ticDuration
	var goal by UserInfoTicsTable.goal
	var followProgress by UserInfoTicsTable.followProgress
	var createdAt by UserInfoTicsTable.createdAt
	var updatedAt by UserInfoTicsTable.updatedAt
}