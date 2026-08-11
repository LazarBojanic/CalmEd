package com.calmed.calmedbackend.model.raw.user

import com.calmed.calmedbackend.model.toRaw
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.*

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<UserEntity>(UserTable)
	var email by UserTable.email
	var username by UserTable.username
	var profileImageUrl by UserTable.profileImageUrl
	var isEmailVerified by UserTable.isEmailVerified
	var isOnboarded by UserTable.isOnboarded
	var isPaid by UserTable.isPaid
	var stripeCustomerId by UserTable.stripeCustomerId
	var createdAt by UserTable.createdAt
	var updatedAt by UserTable.updatedAt
}
