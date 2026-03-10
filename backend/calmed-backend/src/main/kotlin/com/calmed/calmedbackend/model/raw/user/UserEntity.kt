package com.calmed.calmedbackend.model.raw.user

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<UserEntity>(UserTable)
	var email by UserTable.email
	var username by UserTable.username
	var isEmailVerified by UserTable.isEmailVerified
	var isOnboarded by UserTable.isOnboarded
	var isPaid by UserTable.isPaid
	var paymentType by UserTable.paymentType
	var stripeCustomerId by UserTable.stripeCustomerId
	var createdAt by UserTable.createdAt
	var updatedAt by UserTable.updatedAt
}
