package com.calmed.calmedfrontendtourettes.model

import com.calmed.calmedfrontendtourettes.model.dto.response.UserDto
import com.calmed.calmedfrontendtourettes.model.dto.response.UserInfoTourettesDto
import com.calmed.calmedfrontendtourettes.model.joined.UserInfoTourettesJoined
import com.calmed.calmedfrontendtourettes.model.joined.UserJoined
import com.calmed.calmedfrontendtourettes.model.raw.UserEntity
import com.calmed.calmedfrontendtourettes.model.raw.UserInfoTourettesEntity

fun UserDto.toEntity(): UserEntity {
	return UserEntity(
		id = id,
		email = email,
		username = username,
		isEmailVerified = isEmailVerified,
		isOnboarded = isOnboarded,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun UserInfoTourettesDto.toEntity(): UserInfoTourettesEntity {
	return UserInfoTourettesEntity(
		id = id,
		userId = user.id,
		preferredName = preferredName,
		age = age,
		stressLevel = stressLevel,
		tickType = tickType,
		tickFrequency = tickFrequency,
		goal = goal,
		followProgress = followProgress,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun UserEntity.toJoined(): UserJoined {
	return UserJoined(
		id = id,
		email = email,
		username = username,
		isEmailVerified = isEmailVerified,
		isOnboarded = isOnboarded,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun UserInfoTourettesEntity.toJoined(user: UserJoined): UserInfoTourettesJoined {
	return UserInfoTourettesJoined(
		id = id,
		user = user,
		preferredName = preferredName,
		age = age,
		stressLevel = stressLevel,
		tickType = tickType,
		tickFrequency = tickFrequency,
		goal = goal,
		followProgress = followProgress,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}
