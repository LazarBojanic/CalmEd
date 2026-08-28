package com.calmed.calmedtics.model

import com.calmed.calmedtics.model.dto.response.UserDto
import com.calmed.calmedtics.model.dto.response.UserInfoTicsDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.UserEntity
import com.calmed.calmedtics.model.raw.UserInfoTicsEntity

fun UserDto.toEntity(): UserEntity {
	return UserEntity(
		id = id,
		email = email,
		username = username,
		profileImageUrl = profileImageUrl,
		isEmailVerified = isEmailVerified,
		isOnboarded = isOnboarded,
		confirmOverEighteen = confirmOverEighteen,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun UserInfoTicsDto.toEntity(): UserInfoTicsEntity {
	return UserInfoTicsEntity(
		id = id,
		userId = user.id,
		preferredName = preferredName,
		age = age,
		stressLevel = stressLevel,
		tickType = tickType,
		tickFrequency = tickFrequency,
		ticDuration = ticDuration,
		goal = goal,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun UserDto.toJoined(): UserJoined {
	return UserJoined(
		id = id,
		email = email,
		username = username,
		profileImageUrl = profileImageUrl,
		isEmailVerified = isEmailVerified,
		isOnboarded = isOnboarded,
		confirmOverEighteen = confirmOverEighteen,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun UserEntity.toJoined(): UserJoined {
	return UserJoined(
		id = id,
		email = email,
		username = username,
		profileImageUrl = profileImageUrl,
		isEmailVerified = isEmailVerified,
		isOnboarded = isOnboarded,
		confirmOverEighteen = confirmOverEighteen,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun UserInfoTicsEntity.toJoined(user: UserJoined): UserInfoTicsJoined {
	return UserInfoTicsJoined(
		id = id,
		user = user,
		preferredName = preferredName,
		age = age,
		stressLevel = stressLevel,
		tickType = tickType,
		tickFrequency = tickFrequency,
		ticDuration = ticDuration,
		goal = goal,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

