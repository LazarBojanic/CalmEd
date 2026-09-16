package com.calmed.calmedtics.model

import com.calmed.calmedtics.model.dto.response.ExerciseGroupDto
import com.calmed.calmedtics.model.dto.response.ProgramExerciseDto
import com.calmed.calmedtics.model.dto.response.UserDto
import com.calmed.calmedtics.model.dto.response.UserInfoTicsDto
import com.calmed.calmedtics.model.joined.UserInfoTicsJoined
import com.calmed.calmedtics.model.joined.UserJoined
import com.calmed.calmedtics.model.raw.ExerciseGroupEntity
import com.calmed.calmedtics.model.raw.ProgramExerciseEntity
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
		ticType = ticType,
		ticFrequency = ticFrequency,
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

fun ProgramExerciseDto.toEntity(): ProgramExerciseEntity {
	return ProgramExerciseEntity(
		id = id,
		weekNumber = weekNumber,
		groupId = groupId,
		title = title,
		description = description,
		token = token,
		previewToken = previewToken,
		thumbnailToken = thumbnailToken,
		previewThumbnailToken = previewThumbnailToken,
		playbackId = playbackId,
		previewPlaybackId = previewPlaybackId,
		url = url,
		previewURL = previewURL,
		thumbnailURL = thumbnailURL,
		previewThumbnailURL = previewThumbnailURL,
		durationSeconds = durationSeconds,
		visibility = visibility,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun ProgramExerciseEntity.toDto(): ProgramExerciseDto {
	return ProgramExerciseDto(
		id = id,
		weekNumber = weekNumber,
		groupId = groupId,
		title = title,
		description = description,
		token = token,
		previewToken = previewToken,
		thumbnailToken = thumbnailToken,
		previewThumbnailToken = previewThumbnailToken,
		playbackId = playbackId,
		previewPlaybackId = previewPlaybackId,
		url = url,
		previewURL = previewURL,
		thumbnailURL = thumbnailURL,
		previewThumbnailURL = previewThumbnailURL,
		durationSeconds = durationSeconds,
		visibility = visibility,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

fun ExerciseGroupDto.toEntity(): ExerciseGroupEntity {
	return ExerciseGroupEntity(
		id = id,
		name = name,
		description = description
	)
}

fun ExerciseGroupEntity.toDto(): ExerciseGroupDto {
	return ExerciseGroupDto(
		id = id,
		name = name,
		description = description
	)
}

fun UserInfoTicsEntity.toJoined(user: UserJoined): UserInfoTicsJoined {
	return UserInfoTicsJoined(
		id = id,
		user = user,
		preferredName = preferredName,
		age = age,
		stressLevel = stressLevel,
		ticType = ticType,
		ticFrequency = ticFrequency,
		ticDuration = ticDuration,
		goal = goal,
		createdAt = createdAt,
		updatedAt = updatedAt
	)
}

