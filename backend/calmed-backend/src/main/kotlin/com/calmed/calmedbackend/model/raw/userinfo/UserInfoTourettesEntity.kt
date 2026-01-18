package com.calmed.calmedbackend.model.raw.userinfo

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

class UserInfoTourettesEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<UserInfoTourettesEntity>(UserInfoTourettesTable)
	var userId by UserInfoTourettesTable.userId
	var preferredName by UserInfoTourettesTable.preferredName
	var age by UserInfoTourettesTable.age
	var stressLevel by UserInfoTourettesTable.stressLevel
	var tickType by UserInfoTourettesTable.tickType
	var tickFrequency by UserInfoTourettesTable.tickFrequency
	var goal by UserInfoTourettesTable.goal
	var followProgress by UserInfoTourettesTable.followProgress
	var createdAt by UserInfoTourettesTable.createdAt
	var updatedAt by UserInfoTourettesTable.updatedAt
}